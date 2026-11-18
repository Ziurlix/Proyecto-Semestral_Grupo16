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
import com.example.data.model.Client

@Composable
fun ClientsContent(clients: List<Client>) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Clientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Información ficticia para pruebas de la aplicación.", style = MaterialTheme.typography.bodySmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(clients, key = { it.id }) { client ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(client.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(client.company)
                        Text(client.phone, style = MaterialTheme.typography.bodySmall)
                        Text(client.address, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
