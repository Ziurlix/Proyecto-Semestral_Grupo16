package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Delivery
import com.example.data.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationsDao {

    // --- CONSULTAS DE PEDIDOS (Orders) ---
    // Usamos Flow para que la UI reaccione automáticamente a los cambios
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getOrdersFlow(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<Order>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, newStatus: String)

    // --- CONSULTAS DE ENTREGAS (Deliveries) ---
    @Query("SELECT * FROM deliveries ORDER BY id ASC")
    fun getDeliveriesFlow(): Flow<List<Delivery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveries(deliveries: List<Delivery>)

    @Query("UPDATE deliveries SET status = :newStatus, isSyncPending = :isSyncPending WHERE id = :deliveryId")
    suspend fun updateDeliveryStatus(deliveryId: String, newStatus: String, isSyncPending: Boolean)
    
    @Query("UPDATE orders SET isArchived = 1 WHERE id = :orderId")
    suspend fun archiveOrder(orderId: String)

    @Query("UPDATE deliveries SET isArchived = 1 WHERE id = :deliveryId")
    suspend fun archiveDelivery(deliveryId: String)
}