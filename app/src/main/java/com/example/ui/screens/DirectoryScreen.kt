package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.model.Client
import com.example.data.model.Product

@Composable
fun DirectoryContent(products: List<Product>, clients: List<Client>) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Catálogo de Productos", "Directorio de Clientes")

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (selectedTabIndex) {
            0 -> ProductsContent(products)
            1 -> ClientsContent(clients)
        }
    }
}
