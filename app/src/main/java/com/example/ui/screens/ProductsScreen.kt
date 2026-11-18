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
import com.example.data.model.Product
import com.example.ui.utils.formatClp

@Composable
fun ProductsContent(products: List<Product>) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Catálogo de productos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(products, key = { it.id }) { product ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text(product.category, style = MaterialTheme.typography.bodySmall)
                            Text("Stock de prueba: ${product.stock}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(formatClp(product.price), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
