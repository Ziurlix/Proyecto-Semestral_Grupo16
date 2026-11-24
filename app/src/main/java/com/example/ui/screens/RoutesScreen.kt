package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DeliveryRoute

@Composable
fun RoutesContent(routes: List<DeliveryRoute>) {
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
