package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Delivery
import com.example.ui.components.DeliveryCard
import com.example.ui.components.EmptyStateView

import androidx.compose.runtime.*

@Composable
fun DeliveriesContent(
    deliveries: List<Delivery>, 
    canUpdate: Boolean, 
    onUpdate: (String, String) -> Unit, 
    onDelete: (String) -> Unit = {},
    isHistory: Boolean = false
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    
    val filters = if (isHistory) {
        listOf("Todos", "Entregado", "Cancelado", "Pendiente", "En ruta")
    } else {
        listOf("Todos", "Pendiente", "En ruta", "Entregado")
    }

    val baseDeliveries = if (isHistory) {
        deliveries
    } else {
        deliveries.filter { it.status != "Cancelado" && !it.isArchived }
    }

    val filteredDeliveries = if (selectedFilter == "Todos") {
        baseDeliveries
    } else {
        baseDeliveries.filter { it.status.equals(selectedFilter, ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (isHistory) "Historial de Entregas" else "Rutas y entregas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(if (canUpdate && !isHistory) "Actualiza el estado al completar cada etapa." else "Consulta los estados de distribución.", style = MaterialTheme.typography.bodySmall)
        
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                androidx.compose.material3.FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) }
                )
            }
        }

        if (filteredDeliveries.isEmpty()) EmptyStateView("No hay entregas para este filtro.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredDeliveries, key = { it.id }) { delivery ->
                DeliveryCard(
                    delivery, 
                    canUpdate = canUpdate && !isHistory, 
                    onUpdate, 
                    onDelete = if (!isHistory) { { onDelete(delivery.id) } } else null
                )
            }
        }
    }
}
