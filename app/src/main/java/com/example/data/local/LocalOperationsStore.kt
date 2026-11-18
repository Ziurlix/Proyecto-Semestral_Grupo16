package com.example.data.local

import android.content.Context
import com.example.data.model.Delivery
import com.example.data.model.Order
import org.json.JSONArray
import org.json.JSONObject

/** Persistencia local simple con SharedPreferences para el MVP académico. */
class LocalOperationsStore(context: Context) {
    private val preferences = context.getSharedPreferences("ventas_distribucion_store", Context.MODE_PRIVATE)

    fun saveDeliveryStatus(deliveryId: String, status: String) {
        preferences.edit().putString("delivery_$deliveryId", status).apply()
    }

    fun deliveryStatus(deliveryId: String, default: String): String =
        preferences.getString("delivery_$deliveryId", default) ?: default

    fun saveOrders(orders: List<Order>) {
        val array = JSONArray()
        orders.forEach { order ->
            array.put(JSONObject().apply {
                put("id", order.id)
                put("clientName", order.clientName)
                put("productName", order.productName)
                put("quantity", order.quantity)
                put("priority", order.priority)
                put("status", order.status)
                put("date", order.date)
                put("isSyncPending", order.isSyncPending) // NUEVO
            })
        }
        preferences.edit().putString("orders", array.toString()).apply()
    }

    fun loadOrders(): List<Order>? {
        val raw = preferences.getString("orders", null) ?: return null
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                Order(
                    id = item.optString("id"),
                    clientName = item.optString("clientName"),
                    productName = item.optString("productName"),
                    quantity = item.optInt("quantity", 1),
                    priority = item.optString("priority", "Normal"),
                    status = item.optString("status", "Pendiente"),
                    date = item.optString("date", ""),
                    isSyncPending = item.optBoolean("isSyncPending", false) // NUEVO
                )
            }
        }.getOrNull()
    }

    fun applySavedDeliveryStatuses(deliveries: List<Delivery>): List<Delivery> = deliveries.map { delivery ->
        delivery.copy(status = deliveryStatus(delivery.id, delivery.status))
    }

    fun clearDemoData() {
        preferences.edit().clear().apply()
    }

    fun isDarkMode(default: Boolean = false): Boolean = preferences.getBoolean("dark_mode", default)
    fun setDarkMode(enabled: Boolean) = preferences.edit().putBoolean("dark_mode", enabled).apply()
}
