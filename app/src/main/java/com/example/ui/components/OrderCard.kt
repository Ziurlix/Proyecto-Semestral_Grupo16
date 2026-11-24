package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Order
import com.example.ui.utils.getStatusColor

@Composable
fun OrderCard(order: Order, onDelete: (() -> Unit)? = null) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.status,
                        color = getStatusColor(order.status),
                        fontWeight = FontWeight.Bold
                    )
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar pedido", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
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
