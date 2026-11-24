package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Delivery
import com.example.data.model.Order
import com.example.data.model.Sale

@Composable
fun ProfileContent(
    email: String,
    role: String,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onLogout: () -> Unit,
    orders: List<Order>,
    deliveries: List<Delivery>,
    sales: List<Sale>,
    onDeleteDelivery: (String) -> Unit,
    onDeleteOrder: (String) -> Unit
) {
    var showHistoryModal by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(Modifier.weight(1f)) {
                    Text("Sesión activa", style = MaterialTheme.typography.labelMedium)
                    Text(email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Rol: $role", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Text("Ajustes de la cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column {
                if (role == "Repartidor") {
                    ProfileMenuItem(Icons.Default.LocalShipping, "Historial de Entregas", { showHistoryModal = "entregas" })
                } else {
                    ProfileMenuItem(Icons.Default.AttachMoney, "Historial de Ventas", { showHistoryModal = "ventas" })
                    ProfileMenuItem(Icons.Default.ShoppingCart, "Historial de Pedidos", { showHistoryModal = "pedidos" })
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.DarkMode, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Modo Oscuro", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = isDarkMode, onCheckedChange = onToggleDarkMode)
                }
                HorizontalDivider()
                ProfileMenuItem(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión", onLogout, tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showHistoryModal != null) {
        HistoryModal(
            type = showHistoryModal!!,
            orders = orders,
            deliveries = deliveries,
            sales = sales,
            onClose = { showHistoryModal = null },
            onDeleteDelivery = onDeleteDelivery,
            onDeleteOrder = onDeleteOrder
        )
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, text: String, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = tint)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryModal(
    type: String, 
    orders: List<Order>, 
    deliveries: List<Delivery>, 
    sales: List<Sale>, 
    onClose: () -> Unit,
    onDeleteDelivery: (String) -> Unit,
    onDeleteOrder: (String) -> Unit
) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Historial de ${type.replaceFirstChar { it.uppercase() }}") },
                    navigationIcon = {
                        IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, "Volver") }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (type) {
                    "entregas" -> DeliveriesContent(deliveries, canUpdate = false, onUpdate = {_,_->}, onDelete = onDeleteDelivery, isHistory = true)
                    "ventas" -> SalesHistoryContent(sales)
                    "pedidos" -> OrdersContent(orders, canCreate = false, onCreate = {}, onDelete = onDeleteOrder, isHistory = true)
                }
            }
        }
    }
}
