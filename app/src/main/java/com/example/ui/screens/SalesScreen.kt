package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import com.example.data.model.Sale
import com.example.ui.components.EmptyStateView
import com.example.ui.utils.formatClp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesDashboardContent(sales: List<Sale>) {
    val totalRevenue = sales.sumOf { it.amount }
    val totalTransactions = sales.size
    val averageTicket = if (totalTransactions > 0) totalRevenue / totalTransactions else 0
    val salesBySeller = sales.groupBy { it.seller }.mapValues { entry -> entry.value.sumOf { it.amount } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Panel de Ventas y Rendimiento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ingresos Totales", style = MaterialTheme.typography.labelLarge)
                Text(formatClp(totalRevenue), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("$totalTransactions transacciones registradas | Promedio: ${formatClp(averageTicket)}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text("Ventas por Vendedor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (salesBySeller.isEmpty()) {
            EmptyStateView("No hay datos de vendedores registrados.")
        } else {
            salesBySeller.forEach { (seller, amount) ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(seller, fontWeight = FontWeight.SemiBold)
                            Text("Vendedor autorizado", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(formatClp(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Información comercial", fontWeight = FontWeight.SemiBold)
                Text("Las ventas se registran de forma automática al completarse y entregarse los pedidos en terreno.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryContent(sales: List<Sale>) {
    var selectedMonth by remember { mutableStateOf<String?>("Todos") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    val dateFormatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale("es", "CL")) }
    val selectedDateString = selectedDateMillis?.let { dateFormatter.format(Date(it)) }
    
    val availableMonths = listOf("Todos") + sales.map { if (it.date.length >= 7) it.date.substring(3) else it.date }.distinct()

    val filteredSales = if (selectedDateString != null) {
        sales.filter { it.date == selectedDateString }
    } else if (selectedMonth != "Todos" && selectedMonth != null) {
        sales.filter { it.date.endsWith(selectedMonth!!) }
    } else {
        sales
    }
    
    val totalSales = filteredSales.sumOf { it.amount }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ventas totales filtradas", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(formatClp(totalSales), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text("Cálculo basado en pedidos entregados.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Historial de transacciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row {
                if (selectedDateString != null || selectedMonth != "Todos") {
                    IconButton(onClick = { 
                        selectedDateMillis = null 
                        selectedMonth = "Todos"
                    }) { Icon(Icons.Default.Clear, "Limpiar filtro") }
                }
                IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Filtrar fecha") }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableMonths.forEach { month ->
                FilterChip(
                    selected = selectedMonth == month && selectedDateMillis == null,
                    onClick = { 
                        selectedMonth = month
                        selectedDateMillis = null 
                    },
                    label = { Text(if (month == "Todos") "Todos los meses" else month) }
                )
            }
        }

        if (selectedDateString != null) {
            Text("Mostrando ventas del día: $selectedDateString", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        
        if (filteredSales.isEmpty()) {
            EmptyStateView("No hay ventas registradas para la fecha seleccionada.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredSales, key = { it.id }) { sale ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(sale.id, fontWeight = FontWeight.Bold)
                                Text("Pedido ${sale.orderId}", style = MaterialTheme.typography.bodyMedium)
                                Text("Vendedor: ${sale.seller}", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(formatClp(sale.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(sale.date, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
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
