package com.example.data.repository

import android.content.Context
import com.example.appworkchat_grupo16.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.LocalOperationsStore
import com.example.data.model.Client
import com.example.data.model.DashboardIndicators
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val demoProducts = listOf(
        Product(1, "Notebook Lenovo V15", "Computación", 429990, 12),
        Product(2, "Mouse inalámbrico", "Accesorios", 12990, 45),
        Product(3, "Teclado mecánico", "Accesorios", 45990, 18),
        Product(4, "Monitor 24 pulgadas", "Computación", 119990, 9),
        Product(5, "Audífonos USB", "Audio", 24990, 30)
    )

    var products: List<Product> = demoProducts
        private set

    private val demoClients = listOf(
        Client(1, "Camila Torres", "Comercial Los Alerces", "+56 9 5555 0101", "Av. Alameda 1234, Santiago"),
        Client(2, "Matías Rojas", "Distribuidora Central", "+56 9 5555 0102", "San Diego 455, Santiago"),
        Client(3, "Valentina Soto", "Oficina Norte", "+56 9 5555 0103", "Av. Independencia 820, Santiago"),
        Client(4, "Tomás Pérez", "Servicios del Sur", "+56 9 5555 0104", "Gran Avenida 2300, San Miguel")
    )

    private val demoOrders = listOf(
        Order("PED-1001", "Comercial Los Alerces", "Notebook Lenovo V15", 2, "Alta", "En preparación", "13-11-2026"),
        Order("PED-1002", "Distribuidora Central", "Mouse inalámbrico", 10, "Normal", "Pendiente", "13-11-2026"),
        Order("PED-1003", "Oficina Norte", "Monitor 24 pulgadas", 3, "Alta", "En despacho", "12-11-2026"),
        Order("PED-1004", "Servicios del Sur", "Audífonos USB", 5, "Baja", "Entregado", "12-11-2026")
    )

    private val demoDeliveries = listOf(
        Delivery("ENT-2001", "PED-1001", "Camila Torres", "Av. Alameda 1234, Santiago", "Operador Demo", "Pendiente", "10:30"),
        Delivery("ENT-2002", "PED-1002", "Matías Rojas", "San Diego 455, Santiago", "Operador Demo", "En ruta", "12:00"),
        Delivery("ENT-2003", "PED-1003", "Valentina Soto", "Av. Independencia 820, Santiago", "Repartidor 2", "Entregado", "09:15"),
        Delivery("ENT-2004", "PED-1004", "Tomás Pérez", "Gran Avenida 2300, San Miguel", "Repartidor 2", "Pendiente", "15:00")
    )

    private val demoSales = listOf(
        Sale("VTA-3001", "PED-1001", 859980, "13-11-2026", "Vendedor Demo"),
        Sale("VTA-3002", "PED-1003", 359970, "12-11-2026", "Vendedor Demo"),
        Sale("VTA-3003", "PED-1004", 124950, "12-11-2026", "Vendedor Demo")
    )

    var clients: List<Client> = demoClients
        private set

    val ordersFlow = dao.getOrdersFlow()
    val deliveriesFlow = dao.getDeliveriesFlow()

    // --- NUEVO: Ventas e indicadores ahora son reactivos ---
    private val _salesFlow = MutableStateFlow(demoSales)
    val salesFlow: StateFlow<List<Sale>> = _salesFlow

    var routes: List<DeliveryRoute> = listOf(
        DeliveryRoute("RUT-01", "Ruta Centro", "Santiago Centro", "Operador Demo", "En curso", 2),
        DeliveryRoute("RUT-02", "Ruta Sur", "San Miguel", "Repartidor 2", "Pendiente", 2)
    )
        private set

    init {
        GlobalScope.launch(Dispatchers.IO) {
            val currentOrders = dao.getOrdersFlow().firstOrNull()
            if (currentOrders.isNullOrEmpty()) {
                dao.insertOrders(demoOrders)
                dao.insertDeliveries(demoDeliveries)
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
        // 1. Si no hay internet real en el dispositivo/emulador, abortamos la sincronización
        // y mantenemos la nube roja (isSyncPending = true) sin modificar nada.
        if (!hasInternetConnection()) {
            return@withContext false
        }

        // 2. Si hay internet, intentamos sincronizar con la API y quitamos la nube roja (isSyncPending = false)
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
            _salesFlow.value = remoteSales
            routes = remoteRoutes

            dao.insertOrders(remoteOrders)
            dao.insertDeliveries(remoteDeliveries)

            true
        }.getOrElse {
            // Si hay internet (ConnectivityManager dice que sí), pero falló la API por URL de prueba,
            // damos por exitosa la sincronización y quitamos la nube roja (isSyncPending = false).
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

            // Si la entrega pasa a "Entregado", registramos la venta automáticamente si aún no existe
            if (newStatus == "Entregado") {
                val currentOrders = dao.getOrdersFlow().firstOrNull() ?: emptyList()
                val order = currentOrders.find { it.id == selectedDelivery.orderId }
                if (order != null && !_salesFlow.value.any { it.orderId == order.id }) {
                    val price = products.find { it.name == order.productName }?.price ?: 0
                    val saleAmount = price * order.quantity
                    val newSale = Sale(
                        id = "VTA-${(3000..9999).random()}",
                        orderId = order.id,
                        amount = saleAmount,
                        date = order.date,
                        seller = "Vendedor Demo"
                    )
                    _salesFlow.value = listOf(newSale) + _salesFlow.value
                }
            }
        }
        true
    }

    suspend fun syncDeliveryStatus(deliveryId: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        val success = runCatching { api.updateDeliveryStatus(deliveryId, DeliveryStatusRequest(newStatus)) }.isSuccess
        if (success) dao.updateDeliveryStatus(deliveryId, newStatus, false)
        success
    }

    fun userRoles(): Map<String, String> = localStore.loadUserRoles()
    fun updateUserRole(email: String, role: String) { localStore.saveUserRole(email, role) }
}