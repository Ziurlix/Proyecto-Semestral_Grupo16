package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Order
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OrderCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersContent(
    orders: List<Order>, 
    canCreate: Boolean, 
    onCreate: () -> Unit, 
    onDelete: (String) -> Unit,
    isHistory: Boolean = false
) {
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedMonthFilter by remember { mutableStateOf("Todos") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    val dateFormatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale("es", "CL")) }
    val selectedDateString = selectedDateMillis?.let { dateFormatter.format(Date(it)) }

    val statusFilters = if (isHistory) {
        listOf("Todos", "Realizados", "Cancelados", "Pendiente", "En preparación", "En despacho")
    } else {
        listOf("Todos", "Pendiente", "En preparación", "En despacho", "Entregado")
    }

    val availableMonths = listOf("Todos") + orders.map { if (it.date.length >= 7) it.date.substring(3) else it.date }.distinct()

    val baseOrders = if (isHistory) {
        orders
    } else {
        orders.filter { it.status != "Cancelado" && !it.isArchived }
    }

    val filteredOrders = baseOrders.filter { order ->
        val matchesStatus = when (selectedStatusFilter) {
            "Todos" -> true
            "Realizados" -> order.status == "Entregado"
            "Cancelados" -> order.status == "Cancelado"
            else -> order.status.equals(selectedStatusFilter, ignoreCase = true)
        }
        val matchesMonth = selectedMonthFilter == "Todos" || (order.date.length >= 7 && order.date.substring(3) == selectedMonthFilter)
        val matchesDate = selectedDateString == null || order.date == selectedDateString
        matchesStatus && matchesMonth && matchesDate
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (isHistory) "Historial de Pedidos" else "Gestión de pedidos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${filteredOrders.size} pedidos mostrados", style = MaterialTheme.typography.bodySmall)
                }
                if (isHistory) {
                    Row {
                        if (selectedDateString != null || selectedMonthFilter != "Todos") {
                            IconButton(onClick = { 
                                selectedDateMillis = null 
                                selectedMonthFilter = "Todos"
                            }) { Icon(Icons.Default.Clear, "Limpiar filtro fecha") }
                        }
                        IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Filtrar por fecha") }
                    }
                }
            }
            
            // Filtros de Estado
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusFilters.forEach { filter ->
                    FilterChip(
                        selected = selectedStatusFilter == filter,
                        onClick = { selectedStatusFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            // Filtros de Mes (solo para historial)
            if (isHistory) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableMonths.forEach { month ->
                        FilterChip(
                            selected = selectedMonthFilter == month && selectedDateMillis == null,
                            onClick = { 
                                selectedMonthFilter = month
                                selectedDateMillis = null
                            },
                            label = { Text(if (month == "Todos") "Todos los meses" else month) }
                        )
                    }
                }
                if (selectedDateString != null) {
                    Text("Filtrando por día: $selectedDateString", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            if (filteredOrders.isEmpty()) EmptyStateView("No hay pedidos para los filtros seleccionados.")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { 
                items(filteredOrders, key = { it.id }) { 
                    OrderCard(order = it, onDelete = if (!isHistory) { { onDelete(it.id) } } else null) 
                } 
            }
        }
        
        if (canCreate && !isHistory) {
            FloatingActionButton(
                onClick = onCreate,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo pedido")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false 
                }) { Text("Aplicar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
