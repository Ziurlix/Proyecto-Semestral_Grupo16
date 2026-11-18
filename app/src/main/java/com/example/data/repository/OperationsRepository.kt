package com.example.data.repository

import android.content.Context
import com.example.appworkchat_grupo16.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.LocalOperationsStore
import com.example.data.local.MockDataSource
import com.example.data.model.Client
import com.example.data.model.Delivery
import com.example.data.model.DeliveryRoute
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.remote.CreateOrderRequest
import com.example.data.remote.DeliveryStatusRequest
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OperationsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val localStore = LocalOperationsStore(appContext)
    private val api = RetrofitClient.create(BuildConfig.API_BASE_URL)

    private val database = AppDatabase.getDatabase(context)
    private val dao = database.operationsDao()

    private val _productsFlow = MutableStateFlow(MockDataSource.demoProducts)
    
    var products: List<Product>
        get() = _productsFlow.value
        private set(value) { _productsFlow.value = value }

    var clients: List<Client> = MockDataSource.demoClients
        private set

    val ordersFlow = dao.getOrdersFlow()
    val deliveriesFlow = dao.getDeliveriesFlow()

    val salesFlow: Flow<List<Sale>> = combine(ordersFlow, _productsFlow) { orders, prods ->
        orders.filter { it.status.equals("Entregado", ignoreCase = true) }.map { order ->
            val price = prods.find { it.name == order.productName }?.price ?: 0
            Sale(
                id = "VTA-${order.id.removePrefix("PED-")}",
                orderId = order.id,
                amount = price * order.quantity,
                date = order.date,
                seller = "Vendedor Demo"
            )
        }
    }

    var routes: List<DeliveryRoute> = MockDataSource.demoRoutes
        private set

    init {
        GlobalScope.launch(Dispatchers.IO) {
            val currentOrders = dao.getOrdersFlow().firstOrNull()
            if (currentOrders.isNullOrEmpty()) {
                dao.insertOrders(MockDataSource.demoOrders)
                dao.insertDeliveries(MockDataSource.demoDeliveries)
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun refreshFromApi(): Boolean = withContext(Dispatchers.IO) {
        if (!hasInternetConnection()) {
            return@withContext false
        }

        val syncSuccess = runCatching {
            val currentOrders = dao.getOrdersFlow().firstOrNull() ?: emptyList()
            val pendingOrders = currentOrders.filter { it.isSyncPending }

            for (order in pendingOrders) {
                try {
                    api.createOrder(
                        CreateOrderRequest(
                            id = order.id, clientName = order.clientName, productName = order.productName,
                            quantity = order.quantity, priority = order.priority, status = order.status, date = order.date
                        )
                    )
                } catch (e: Exception) {}
                dao.insertOrder(order.copy(isSyncPending = false))
            }

            val currentDeliveries = dao.getDeliveriesFlow().firstOrNull() ?: emptyList()
            for (delivery in currentDeliveries.filter { it.isSyncPending }) {
                try {
                    api.updateDeliveryStatus(delivery.id, DeliveryStatusRequest(delivery.status))
                } catch (e: Exception) {}
                dao.updateDeliveryStatus(delivery.id, delivery.status, false)
            }

            val remoteProducts = api.getProducts().map { Product(it.id, it.name, it.category, it.price, it.stock) }
            val remoteClients = api.getClients().map { Client(it.id, it.name, it.company, it.phone, it.address) }
            val remoteOrders = api.getOrders().map { Order(it.id, it.clientName, it.productName, it.quantity, it.priority, it.status, it.date) }
            val remoteDeliveries = api.getDeliveries().map { Delivery(it.id, it.orderId, it.recipient, it.address, it.driver, it.status, it.scheduledTime) }
            val remoteSales = api.getSales().map { Sale(it.id, it.orderId, it.amount, it.date, it.seller) }
            val remoteRoutes = api.getRoutes().map { DeliveryRoute(it.id, it.name, it.zone, it.driver, it.status, it.deliveriesCount) }

            products = remoteProducts
            clients = remoteClients
            routes = remoteRoutes

            dao.insertOrders(remoteOrders)
            dao.insertDeliveries(remoteDeliveries)

            true
        }.getOrElse {
            val currentOrders = dao.getOrdersFlow().firstOrNull() ?: emptyList()
            currentOrders.filter { it.isSyncPending }.forEach { order ->
                dao.insertOrder(order.copy(isSyncPending = false))
            }
            val currentDeliveries = dao.getDeliveriesFlow().firstOrNull() ?: emptyList()
            currentDeliveries.filter { it.isSyncPending }.forEach { delivery ->
                dao.updateDeliveryStatus(delivery.id, delivery.status, false)
            }
            true
        }

        syncSuccess
    }

    suspend fun createOrder(clientName: String, productName: String, quantity: Int, priority: String): Order {
        return withContext(Dispatchers.IO) {
            val currentOrders = dao.getOrdersFlow().firstOrNull() ?: emptyList()
            val nextNumber = 1000 + currentOrders.size + 1
            val order = Order(
                id = "PED-$nextNumber",
                clientName = clientName,
                productName = productName,
                quantity = quantity.coerceAtLeast(1),
                priority = priority,
                status = "Pendiente",
                date = SimpleDateFormat("dd-MM-yyyy", Locale("es", "CL")).format(Date()),
                isSyncPending = true
            )
            dao.insertOrder(order)
            order
        }
    }

    suspend fun syncCreatedOrder(order: Order): Boolean = withContext(Dispatchers.IO) {
        val success = runCatching {
            api.createOrder(CreateOrderRequest(order.id, order.clientName, order.productName, order.quantity, order.priority, order.status, order.date))
        }.isSuccess
        if (success) dao.insertOrder(order.copy(isSyncPending = false))
        success
    }

    suspend fun updateDeliveryStatusLocal(deliveryId: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        val currentDeliveries = dao.getDeliveriesFlow().firstOrNull() ?: return@withContext false
        val selectedDelivery = currentDeliveries.firstOrNull { it.id == deliveryId } ?: return@withContext false

        dao.updateDeliveryStatus(deliveryId, newStatus, true)
        val orderStatus = when (newStatus) {
            "En ruta" -> "En despacho"
            "Entregado" -> "Entregado"
            else -> null
        }
        if (orderStatus != null) {
            dao.updateOrderStatus(selectedDelivery.orderId, orderStatus)
        }
        true
    }

    suspend fun deleteDeliveryLocal(deliveryId: String) = withContext(Dispatchers.IO) {
        val currentDeliveries = dao.getDeliveriesFlow().firstOrNull() ?: emptyList()
        val delivery = currentDeliveries.find { it.id == deliveryId } ?: return@withContext
        if (delivery.status == "Entregado") {
            dao.archiveDelivery(deliveryId)
        } else {
            dao.updateDeliveryStatus(deliveryId, "Cancelado", true)
            dao.updateOrderStatus(delivery.orderId, "Cancelado")
        }
    }

    suspend fun deleteOrderLocal(orderId: String) = withContext(Dispatchers.IO) {
        val currentOrders = dao.getOrdersFlow().firstOrNull() ?: emptyList()
        val order = currentOrders.find { it.id == orderId } ?: return@withContext
        if (order.status == "Entregado") {
            dao.archiveOrder(orderId)
        } else {
            dao.updateOrderStatus(orderId, "Cancelado")
            
            // Also cancel the associated delivery if exists
            val currentDeliveries = dao.getDeliveriesFlow().firstOrNull() ?: emptyList()
            currentDeliveries.find { it.orderId == orderId }?.let { delivery ->
                if (delivery.status != "Entregado") {
                    dao.updateDeliveryStatus(delivery.id, "Cancelado", true)
                }
            }
        }
    }

    suspend fun syncDeliveryStatus(deliveryId: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        val success = runCatching { api.updateDeliveryStatus(deliveryId, DeliveryStatusRequest(newStatus)) }.isSuccess
        if (success) dao.updateDeliveryStatus(deliveryId, newStatus, false)
        success
    }
}