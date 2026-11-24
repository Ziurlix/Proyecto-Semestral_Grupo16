package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Delivery
import com.example.data.model.Order
import com.example.ui.components.MetricCard
import com.example.ui.components.OrderCard
import com.example.viewmodel.DashboardViewModel

@Composable
fun SummaryContent(viewModel: DashboardViewModel, orders: List<Order>, deliveries: List<Delivery>) {
    val totalOrders = orders.size
    val pendingOrders = orders.count { it.status != "Entregado" }
    val pendingDeliveries = deliveries.count { it.status != "Entregado" }
    val delivered = deliveries.count { it.status == "Entregado" }

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
        Text("Indicadores logísticos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Pedidos totales", totalOrders.toString(), Modifier.weight(1f))
            MetricCard("Pendientes", pendingOrders.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Entregas a realizar", pendingDeliveries.toString(), Modifier.weight(1f))
            MetricCard("Completadas", delivered.toString(), Modifier.weight(1f))
        }
        
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cumplimiento de entregas", fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(
                    progress = { if (deliveries.isEmpty()) 0f else delivered.toFloat() / deliveries.size },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("$delivered de ${deliveries.size} entregas finalizadas", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        Text("Actividad reciente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        orders.take(3).forEach { order -> OrderCard(order) }
        Spacer(Modifier.height(8.dp))
    }
}
