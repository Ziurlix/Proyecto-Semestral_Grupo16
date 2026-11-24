package com.example.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Delete
import com.example.data.model.Delivery
import com.example.ui.utils.getStatusColor

@Composable
fun DeliveryCard(delivery: Delivery, canUpdate: Boolean, onUpdate: (String, String) -> Unit, onDelete: (() -> Unit)? = null) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = delivery.status,
                        color = getStatusColor(delivery.status),
                        fontWeight = FontWeight.Bold
                    )
                    if (onDelete != null && (delivery.status == "Entregado" || delivery.status == "Cancelado")) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar entrega", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
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
