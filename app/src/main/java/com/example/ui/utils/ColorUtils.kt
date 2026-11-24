package com.example.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
