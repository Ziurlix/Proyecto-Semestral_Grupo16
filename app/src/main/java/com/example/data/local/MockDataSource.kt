package com.example.data.local

import com.example.data.model.Client
import com.example.data.model.Delivery
import com.example.data.model.DeliveryRoute
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Sale

object MockDataSource {
    val demoProducts = listOf(
        Product(1, "Notebook Lenovo V15", "Computación", 429990, 12),
        Product(2, "Mouse inalámbrico", "Accesorios", 12990, 45),
        Product(3, "Teclado mecánico", "Accesorios", 45990, 18),
        Product(4, "Monitor 24 pulgadas", "Computación", 119990, 9),
        Product(5, "Audífonos USB", "Audio", 24990, 30)
    )

    val demoClients = listOf(
        Client(1, "Camila Torres", "Comercial Los Alerces", "+56 9 5555 0101", "Av. Alameda 1234, Santiago"),
        Client(2, "Matías Rojas", "Distribuidora Central", "+56 9 5555 0102", "San Diego 455, Santiago"),
        Client(3, "Valentina Soto", "Oficina Norte", "+56 9 5555 0103", "Av. Independencia 820, Santiago"),
        Client(4, "Tomás Pérez", "Servicios del Sur", "+56 9 5555 0104", "Gran Avenida 2300, San Miguel")
    )

    val demoOrders = listOf(
        Order("PED-1001", "Comercial Los Alerces", "Notebook Lenovo V15", 2, "Alta", "En preparación", "13-11-2026"),
        Order("PED-1002", "Distribuidora Central", "Mouse inalámbrico", 10, "Normal", "Pendiente", "13-11-2026"),
        Order("PED-1003", "Oficina Norte", "Monitor 24 pulgadas", 3, "Alta", "En despacho", "12-11-2026"),
        Order("PED-1004", "Servicios del Sur", "Audífonos USB", 5, "Baja", "Entregado", "12-11-2026")
    )

    val demoDeliveries = listOf(
        Delivery("ENT-2001", "PED-1001", "Camila Torres", "Av. Alameda 1234, Santiago", "Operador Demo", "Pendiente", "10:30"),
        Delivery("ENT-2002", "PED-1002", "Matías Rojas", "San Diego 455, Santiago", "Operador Demo", "En ruta", "12:00"),
        Delivery("ENT-2003", "PED-1003", "Valentina Soto", "Av. Independencia 820, Santiago", "Repartidor 2", "Entregado", "09:15"),
        Delivery("ENT-2004", "PED-1004", "Tomás Pérez", "Gran Avenida 2300, San Miguel", "Repartidor 2", "Pendiente", "15:00")
    )

    val demoSales = listOf(
        Sale("VTA-3001", "PED-1001", 859980, "13-11-2026", "Vendedor Demo"),
        Sale("VTA-3002", "PED-1003", 359970, "12-11-2026", "Vendedor Demo"),
        Sale("VTA-3003", "PED-1004", 124950, "12-11-2026", "Vendedor Demo")
    )

    val demoRoutes = listOf(
        DeliveryRoute("RUT-01", "Ruta Centro", "Santiago Centro", "Operador Demo", "En curso", 2),
        DeliveryRoute("RUT-02", "Ruta Sur", "San Miguel", "Repartidor 2", "Pendiente", 2)
    )
}
