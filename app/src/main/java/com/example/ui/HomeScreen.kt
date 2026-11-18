package com.example.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.viewmodel.DashboardViewModel

private data class DashboardTab(val title: String, val shortLabel: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    role: String,
    viewModel: DashboardViewModel,
    onCerrarSesion: () -> Unit
) {
    val tabs = when (role) {
        "Administrador" -> listOf(
            DashboardTab("Resumen", "Inicio", Icons.Default.Home),
            DashboardTab("Pedidos", "Pedidos", Icons.Default.ShoppingCart),
            DashboardTab("Rutas", "Rutas", Icons.Default.Map),
            DashboardTab("Entregas", "Entregas", Icons.Default.LocalShipping),
            DashboardTab("Usuarios", "Usuarios", Icons.Default.People)
        )
        "Supervisor" -> listOf(
            DashboardTab("Resumen", "Inicio", Icons.Default.Home),
            DashboardTab("Pedidos", "Pedidos", Icons.Default.ShoppingCart),
            DashboardTab("Ventas", "Ventas", Icons.Default.AttachMoney),
            DashboardTab("Rutas", "Rutas", Icons.Default.Map),
            DashboardTab("Entregas", "Entregas", Icons.Default.LocalShipping)
        )
        "Vendedor" -> listOf(
            DashboardTab("Resumen", "Inicio", Icons.Default.Home),
            DashboardTab("Pedidos", "Pedidos", Icons.Default.ShoppingCart),
            DashboardTab("Clientes", "Clientes", Icons.Default.Person),
            DashboardTab("Catálogo", "Catálogo", Icons.Default.Inventory),
            DashboardTab("Ventas", "Ventas", Icons.Default.AttachMoney)
        )
        else -> listOf(
            DashboardTab("Resumen", "Inicio", Icons.Default.Home),
            DashboardTab("Rutas", "Rutas", Icons.Default.Map),
            DashboardTab("Entregas", "Entregas", Icons.Default.LocalShipping)
        )
    }

    var selectedTab by rememberSaveable(role) { mutableIntStateOf(0) }
    var showOrderDialog by rememberSaveable { mutableStateOf(false) }
    val snackHost = remember { SnackbarHostState() }

    // --- NUEVO: Escuchamos los flujos reactivos desde el ViewModel ---
    val orders by viewModel.ordersFlow.collectAsState(initial = emptyList())
    val deliveries by viewModel.deliveriesFlow.collectAsState(initial = emptyList())
    val sales by viewModel.salesFlow.collectAsState(initial = emptyList()) // <-- NUEVO

    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            snackHost.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Logística en Terreno", fontWeight = FontWeight.Bold)
                        Text("Sesión: $role", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.sincronizar() }, enabled = !viewModel.syncing) {
                        Text(if (viewModel.syncing) "Sync..." else "Sincronizar")
                    }
                    TextButton(onClick = onCerrarSesion) { Text("Salir") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.shortLabel) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackHost) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (tabs[selectedTab].title) {
                "Resumen" -> SummaryContent(role, viewModel, orders, deliveries, sales) // Pasamos sales
                "Pedidos" -> OrdersContent(orders = orders, canCreate = role == "Vendedor", onCreate = { showOrderDialog = true })
                "Clientes" -> ClientsContent(viewModel.clients)
                "Ventas" -> SalesContent(if (role == "Vendedor") sales.filter { it.seller == "Vendedor Demo" } else sales) // Usa sales
                "Rutas" -> RoutesContent(routes = if (role == "Repartidor") viewModel.routes.filter { it.driver == "Operador Demo" } else viewModel.routes)
                "Entregas" -> DeliveriesContent(deliveries = if (role == "Repartidor") deliveries.filter { it.driver == "Operador Demo" } else deliveries, canUpdate = role == "Repartidor", onUpdate = viewModel::actualizarEntrega)
                "Catálogo" -> ProductsContent(viewModel.products)
                "Usuarios" -> UsersContent(viewModel.userRoles, viewModel::cambiarRolUsuario)
            }
        }
    }

    if (showOrderDialog) {
        CreateOrderDialog(
            products = viewModel.products,
            clients = viewModel.clients,
            onDismiss = { showOrderDialog = false },
            onCreate = { client, product, quantity, priority ->
                // Corregido: ya no espera un booleano porque es una corrutina
                viewModel.crearPedido(client, product, quantity, priority)
                showOrderDialog = false
            }
        )
    }
}

@Composable
private fun SummaryContent(role: String, viewModel: DashboardViewModel, orders: List<Order>, deliveries: List<Delivery>, sales: List<Sale>) {
    val totalOrders = orders.size
    val pendingOrders = orders.count { it.status != "Entregado" }
    val pendingDeliveries = deliveries.count { it.status != "Entregado" }
    val delivered = deliveries.count { it.status == "Entregado" }
    val totalSales = sales.sumOf { it.amount } // Calcula las ventas reactivas reales

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Panel operativo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Consulta información comercial y logística según los permisos de tu perfil.")
                Text(
                    if (viewModel.apiConnected) "Fuente: API REST" else "Fuente: datos ficticios de demostración",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text("Indicadores principales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Pedidos", totalOrders.toString(), Modifier.weight(1f))
            MetricCard("Pedidos pendientes", pendingOrders.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Entregas pendientes", pendingDeliveries.toString(), Modifier.weight(1f))
            MetricCard("Entregados", delivered.toString(), Modifier.weight(1f))
        }
        MetricCard("Clientes registrados", viewModel.clients.size.toString(), Modifier.fillMaxWidth())
        if (role == "Administrador" || role == "Supervisor") {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ventas registradas (muestra)", fontWeight = FontWeight.SemiBold)
                    Text(formatClp(totalSales), style = MaterialTheme.typography.headlineMedium)
                    Text("Monto ilustrativo calculado con datos sintéticos.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cumplimiento de entregas", fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(
                    progress = { if (deliveries.isEmpty()) 0f else delivered.toFloat() / deliveries.size },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("$delivered de ${deliveries.size} entregas finalizadas")
            }
        }
        Text("Actividad reciente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        orders.take(3).forEach { order -> OrderCard(order) }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun OrdersContent(orders: List<Order>, canCreate: Boolean, onCreate: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Gestión de pedidos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${orders.size} pedidos de prueba", style = MaterialTheme.typography.bodySmall)
            }
            if (canCreate) Button(onClick = onCreate) { Text("Nuevo pedido") }
        }
        if (orders.isEmpty()) EmptyStateView("No hay pedidos registrados.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(orders, key = { it.id }) { OrderCard(it) } }
    }
}

@Composable
private fun OrderCard(order: Order) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(order.id, fontWeight = FontWeight.Bold)
                    if (order.isSyncPending) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Pendiente de subir",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = order.status,
                    color = getStatusColor(order.status),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(order.clientName, style = MaterialTheme.typography.titleMedium)
            Text("${order.productName} · Cantidad: ${order.quantity}")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Prioridad: ${order.priority}", style = MaterialTheme.typography.bodySmall)
                Text(order.date, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ClientsContent(clients: List<Client>) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Clientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Información ficticia para pruebas de la aplicación.", style = MaterialTheme.typography.bodySmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(clients, key = { it.id }) { client ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(client.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(client.company)
                        Text(client.phone, style = MaterialTheme.typography.bodySmall)
                        Text(client.address, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductsContent(products: List<Product>) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Catálogo de productos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(products, key = { it.id }) { product ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text(product.category, style = MaterialTheme.typography.bodySmall)
                            Text("Stock de prueba: ${product.stock}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(formatClp(product.price), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesContent(sales: List<Sale>) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ventas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Registros de ventas ficticios.", style = MaterialTheme.typography.bodySmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sales, key = { it.id }) { sale ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(sale.id, fontWeight = FontWeight.Bold)
                        Text("Pedido ${sale.orderId}")
                        Text("Monto: ${formatClp(sale.amount)}", style = MaterialTheme.typography.titleMedium)
                        Text("Vendedor: ${sale.seller} · Fecha: ${sale.date}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutesContent(routes: List<DeliveryRoute>) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Rutas asignadas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(routes, key = { it.id }) { route ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${route.id} · ${route.name}", fontWeight = FontWeight.Bold)
                        Text("Zona: ${route.zone}")
                        Text("Repartidor: ${route.driver}")
                        Text("Entregas: ${route.deliveriesCount} · Estado: ${route.status}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun formatClp(amount: Int): String =
    "\$" + amount.toString().reversed().chunked(3).joinToString(".").reversed() + " CLP"

@Composable
private fun DeliveriesContent(deliveries: List<Delivery>, canUpdate: Boolean, onUpdate: (String, String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Rutas y entregas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(if (canUpdate) "Actualiza el estado al completar cada etapa." else "Consulta los estados de distribución.", style = MaterialTheme.typography.bodySmall)
        if (deliveries.isEmpty()) EmptyStateView("No hay entregas asignadas en este momento.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(deliveries, key = { it.id }) { delivery ->
                DeliveryCard(delivery, canUpdate, onUpdate)
            }
        }
    }
}

@Composable
private fun DeliveryCard(delivery: Delivery, canUpdate: Boolean, onUpdate: (String, String) -> Unit) {
    val nextStatus = when (delivery.status) {
        "Pendiente" -> "En ruta"
        "En ruta" -> "Entregado"
        else -> null
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onUpdate(delivery.id, "Entregado")
        }
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(delivery.id, fontWeight = FontWeight.Bold)
                    if (delivery.isSyncPending) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Pendiente de subir",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = delivery.status,
                    color = getStatusColor(delivery.status),
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Pedido ${delivery.orderId} · ${delivery.recipient}", fontWeight = FontWeight.Medium)
            Text(delivery.address, style = MaterialTheme.typography.bodySmall)
            Text("Repartidor: ${delivery.driver} · Horario: ${delivery.scheduledTime}", style = MaterialTheme.typography.bodySmall)

            if (canUpdate && nextStatus != null) {
                Button(
                    onClick = {
                        if (nextStatus == "Entregado") {
                            cameraLauncher.launch(null)
                        } else {
                            onUpdate(delivery.id, nextStatus)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (nextStatus == "Entregado") "📸 Capturar evidencia y entregar" else "Marcar como: $nextStatus")
                }
            }

            if (delivery.status == "Entregado") {
                Text("✓ Evidencia fotográfica registrada", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
private fun UsersContent(userRoles: Map<String, String>, onRoleChange: (String, String) -> Unit) {
    val roleOptions = listOf("Supervisor", "Vendedor", "Repartidor")
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Usuarios y roles", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Cambia el perfil de las cuentas de prueba. El cambio se guarda localmente y se aplica en el próximo inicio de sesión.", style = MaterialTheme.typography.bodySmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(userRoles.entries.toList(), key = { it.key }) { entry ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(entry.key, fontWeight = FontWeight.SemiBold)
                        Text(entry.value, color = MaterialTheme.colorScheme.primary)
                        if (entry.key != "admin@guardian.test") {
                            val nextRole = roleOptions[(roleOptions.indexOf(entry.value) + 1).let { if (it < 0) 0 else it % roleOptions.size }]
                            OutlinedButton(onClick = { onRoleChange(entry.key, nextRole) }) {
                                Text("Cambiar a $nextRole")
                            }
                        } else {
                            Text("Cuenta principal protegida", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOrderDialog(
    products: List<Product>,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onCreate: (String, String, Int, String) -> Unit
) {
    var client by remember { mutableStateOf(clients.firstOrNull()?.company ?: "") }
    var product by remember { mutableStateOf(products.firstOrNull()?.name ?: "") }
    var quantityText by remember { mutableStateOf("1") }
    var priority by remember { mutableStateOf("Normal") }
    var showClients by remember { mutableStateOf(false) }
    var showProducts by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar pedido") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = showClients,
                    onExpandedChange = { showClients = !showClients }
                ) {
                    OutlinedTextField(
                        value = client.ifBlank { "Seleccionar cliente" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cliente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showClients) },
                        modifier = Modifier.menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showClients,
                        onDismissRequest = { showClients = false }
                    ) {
                        clients.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.company) },
                                onClick = {
                                    client = item.company
                                    showClients = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = showProducts,
                    onExpandedChange = { showProducts = !showProducts }
                ) {
                    OutlinedTextField(
                        value = product.ifBlank { "Seleccionar producto" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Producto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProducts) },
                        modifier = Modifier.menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showProducts,
                        onDismissRequest = { showProducts = false }
                    ) {
                        products.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    product = item.name
                                    showProducts = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { character -> character.isDigit() }.take(4) },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Prioridad", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Baja", "Normal", "Alta").forEach { value ->
                        FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(value) })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(client, product, quantityText.toIntOrNull() ?: 0, priority) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun EmptyStateView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "entregado" -> Color(0xFF2E7D32)
        "en ruta", "en despacho" -> Color(0xFFEF6C00)
        "en preparación" -> Color(0xFF1565C0)
        "pendiente" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
}