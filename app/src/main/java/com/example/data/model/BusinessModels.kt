package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


/** Datos sintéticos utilizados exclusivamente para la demostración académica. */
data class Client(
    val id: Int,
    val name: String,
    val company: String,
    val phone: String,
    val address: String
)

data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Int,
    val stock: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    val id: String,
    val clientName: String,
    val productName: String,
    val quantity: Int,
    val priority: String,
    val status: String,
    val date: String,
    val isSyncPending: Boolean = false,
    val isArchived: Boolean = false // Permite ocultar el pedido sin perder el dinero en ventas
) {
    val total: Int get() = quantity * (productCatalogPrice[productName] ?: 0)
}

@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey
    val id: String,
    val orderId: String,
    val recipient: String,
    val address: String,
    val driver: String,
    val status: String,
    val scheduledTime: String,
    val isSyncPending: Boolean = false, // NUEVO
    val isArchived: Boolean = false
)

data class Sale(
    val id: String,
    val orderId: String,
    val amount: Int,
    val date: String,
    val seller: String
)

data class AppUser(
    val email: String,
    val role: String,
    val displayName: String
)

val productCatalogPrice = mapOf(
    "Notebook Lenovo V15" to 429990,
    "Mouse inalámbrico" to 12990,
    "Teclado mecánico" to 45990,
    "Monitor 24 pulgadas" to 119990,
    "Audífonos USB" to 24990
)

data class DeliveryRoute(
    val id: String,
    val name: String,
    val zone: String,
    val driver: String,
    val status: String,
    val deliveriesCount: Int
)

data class DashboardIndicators(
    val salesAmount: Int,
    val goalCompletionPercent: Int,
    val pendingOrders: Int,
    val completedDeliveries: Int
)
