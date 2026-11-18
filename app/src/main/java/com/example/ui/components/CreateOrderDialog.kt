package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Client
import com.example.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderDialog(
    products: List<Product>,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onCreate: (String, String, Int, String) -> Unit
) {
    var client by remember { mutableStateOf(clients.firstOrNull()?.company ?: "") }
    var product by remember { mutableStateOf(products.firstOrNull()?.name ?: "") }
    var quantityText by remember { mutableStateOf("1") }
    var priority by remember { mutableStateOf("Normal") }
    var showClients by remember { mutableStateOf(false) }
    var showProducts by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar pedido") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = showClients,
                    onExpandedChange = { showClients = !showClients }
                ) {
                    OutlinedTextField(
                        value = client.ifBlank { "Seleccionar cliente" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cliente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showClients) },
                        modifier = Modifier.menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showClients,
                        onDismissRequest = { showClients = false }
                    ) {
                        clients.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.company) },
                                onClick = {
                                    client = item.company
                                    showClients = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = showProducts,
                    onExpandedChange = { showProducts = !showProducts }
                ) {
                    OutlinedTextField(
                        value = product.ifBlank { "Seleccionar producto" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Producto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProducts) },
                        modifier = Modifier.menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showProducts,
                        onDismissRequest = { showProducts = false }
                    ) {
                        products.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    product = item.name
                                    showProducts = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { character -> character.isDigit() }.take(4) },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Prioridad", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Baja", "Normal", "Alta").forEach { value ->
                        FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(value) })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(client, product, quantityText.toIntOrNull() ?: 0, priority) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
