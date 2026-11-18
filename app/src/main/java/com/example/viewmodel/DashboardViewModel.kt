package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Client
import com.example.data.model.Delivery
import com.example.data.model.DeliveryRoute
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.repository.OperationsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OperationsRepository(application)

    val ordersFlow: StateFlow<List<Order>> = repository.ordersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveriesFlow: StateFlow<List<Delivery>> = repository.deliveriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesFlow: StateFlow<List<Sale>> = repository.salesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var clients by mutableStateOf(repository.clients)
        private set
    var products by mutableStateOf(repository.products)
        private set
    val routes: List<DeliveryRoute> get() = repository.routes

    var apiConnected by mutableStateOf(false)
        private set
    var syncing by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    init { sincronizar() }

    fun sincronizar() {
        if (syncing) return
        viewModelScope.launch {
            syncing = true
            apiConnected = repository.refreshFromApi()
            products = repository.products
            clients = repository.clients
            syncing = false
            //  se ejecutó una simulación local exitosa si no hay API
            message = if (apiConnected) "Información sincronizada con la API." else "Sincronización simulada localmente (Modo Demostración)."
        }
    }

    fun crearPedido(cliente: String, producto: String, cantidad: Int, prioridad: String) {
        if (cliente.isBlank() || producto.isBlank() || cantidad < 1) {
            message = "Completa el cliente, producto y cantidad."
            return
        }
        viewModelScope.launch {
            val newOrder = repository.createOrder(cliente, producto, cantidad, prioridad)
            message = "Pedido registrado y guardado localmente."
            if (apiConnected) {
                repository.syncCreatedOrder(newOrder)
            }
        }
    }

    fun actualizarEntrega(deliveryId: String, nuevoEstado: String) {
        viewModelScope.launch {
            val updated = repository.updateDeliveryStatusLocal(deliveryId, nuevoEstado)
            if (updated) {
                message = "Estado actualizado localmente."
                if (apiConnected) {
                    repository.syncDeliveryStatus(deliveryId, nuevoEstado)
                }
            }
        }
    }

    fun eliminarPedido(orderId: String) {
        viewModelScope.launch {
            repository.deleteOrderLocal(orderId)
            message = "Pedido eliminado."
        }
    }

    fun eliminarEntrega(deliveryId: String) {
        viewModelScope.launch {
            repository.deleteDeliveryLocal(deliveryId)
            message = "Entrega eliminada del historial."
        }
    }

    fun dismissMessage() { message = null }
}