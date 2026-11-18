package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.CreateOrderDialog
import com.example.ui.components.DashboardTab
import com.example.ui.screens.*
import com.example.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    email: String,
    role: String,
    viewModel: DashboardViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val tabs = when (role) {
        "Administrador" -> listOf(
            DashboardTab("Resumen", "Inicio", Icons.Default.Home),
            DashboardTab("Pedidos", "Pedidos", Icons.Default.ShoppingCart),
            DashboardTab("Rutas", "Rutas", Icons.Default.Map),
            DashboardTab("Entregas", "Entregas", Icons.Default.LocalShipping),
            DashboardTab("Perfil", "Perfil", Icons.Default.Person)
        )
        "Supervisor" -> listOf(
            DashboardTab("Resumen", "Inicio", Icons.Default.Home),
            DashboardTab("Pedidos", "Pedidos", Icons.Default.ShoppingCart),
            DashboardTab("Ventas", "Ventas", Icons.Default.AttachMoney),
            DashboardTab("Rutas", "Rutas", Icons.Default.Map),
            DashboardTab("Perfil", "Perfil", Icons.Default.Person)
        )
        "Vendedor" -> listOf(
            DashboardTab("Resumen", "Inicio", Icons.Default.Home),
            DashboardTab("Pedidos", "Pedidos", Icons.Default.ShoppingCart),
            DashboardTab("Directorio", "Directorio", Icons.AutoMirrored.Filled.MenuBook),
            DashboardTab("Ventas", "Ventas", Icons.Default.AttachMoney),
            DashboardTab("Perfil", "Perfil", Icons.Default.Person)
        )
        else -> listOf(
            DashboardTab("Resumen", "Inicio", Icons.Default.Home),
            DashboardTab("Rutas", "Rutas", Icons.Default.Map),
            DashboardTab("Entregas", "Entregas", Icons.Default.LocalShipping),
            DashboardTab("Perfil", "Perfil", Icons.Default.Person)
        )
    }

    var selectedTab by rememberSaveable(role) { mutableIntStateOf(0) }
    var showOrderDialog by rememberSaveable { mutableStateOf(false) }
    val snackHost = remember { SnackbarHostState() }

    val orders by viewModel.ordersFlow.collectAsState(initial = emptyList())
    val deliveries by viewModel.deliveriesFlow.collectAsState(initial = emptyList())
    val sales by viewModel.salesFlow.collectAsState(initial = emptyList())

    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            snackHost.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

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
                "Resumen" -> SummaryContent(viewModel, orders, deliveries)
                "Pedidos" -> OrdersContent(
                    orders = orders,
                    canCreate = role == "Vendedor",
                    onCreate = { showOrderDialog = true },
                    onDelete = viewModel::eliminarPedido,
                    isHistory = false
                )
                "Directorio" -> DirectoryContent(viewModel.products, viewModel.clients)
                "Ventas" -> SalesDashboardContent(if (role == "Vendedor") sales.filter { it.seller == "Vendedor Demo" } else sales)
                "Rutas" -> RoutesContent(routes = if (role == "Repartidor") viewModel.routes.filter { it.driver == "Operador Demo" } else viewModel.routes)
                "Entregas" -> DeliveriesContent(deliveries = if (role == "Repartidor") deliveries.filter { it.driver == "Operador Demo" } else deliveries, canUpdate = role == "Repartidor", onUpdate = viewModel::actualizarEntrega)
                "Perfil" -> ProfileContent(email, role, isDarkMode, onToggleDarkMode, onLogout = { showLogoutDialog = true }, orders = orders, deliveries = deliveries, sales = sales, onDeleteDelivery = viewModel::eliminarEntrega, onDeleteOrder = viewModel::eliminarPedido)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas salir de tu cuenta?") },
            confirmButton = {
                Button(onClick = {
                    showLogoutDialog = false
                    onCerrarSesion()
                }) { Text("Sí, Salir") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showOrderDialog) {
        CreateOrderDialog(
            products = viewModel.products,
            clients = viewModel.clients,
            onDismiss = { showOrderDialog = false },
            onCreate = { client, product, quantity, priority ->
                viewModel.crearPedido(client, product, quantity, priority)
                showOrderDialog = false
            }
        )
    }
}
