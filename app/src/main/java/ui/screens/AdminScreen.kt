package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isport.model.Facility
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AdminScreen(nav: NavController) {
    val db = FirebaseFirestore.getInstance()
    var facilities by remember { mutableStateOf(listOf<Facility>()) }
    var name by remember { mutableStateOf("") }
    var openHour by remember { mutableStateOf("8") }
    var closeHour by remember { mutableStateOf("20") }
    var error by remember { mutableStateOf<String?>(null) }

    // Listener Firestore che recupera e aggiorna i campi in tempo reale
    LaunchedEffect(true) {
        db.collection("facilities")
            .addSnapshotListener { snap, _ ->
                facilities = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Facility::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
    }

    // Layout principale centrato
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pannello Admin", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))

            // Form per aggiungere un nuovo campo
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome campo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = openHour,
                onValueChange = { openHour = it },
                label = { Text("Ora apertura (es. 8)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = closeHour,
                onValueChange = { closeHour = it },
                label = { Text("Ora chiusura (es. 20)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(16.dp))

            // Messaggi di errore
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            // Pulsante per aggiungere un nuovo campo
            Button(
                onClick = {
                    val open = openHour.toIntOrNull()
                    val close = closeHour.toIntOrNull()
                    if (name.isBlank() || open == null || close == null || open >= close) {
                        error = "Dati non validi"
                        return@Button
                    }

                    val facility = mapOf(
                        "name" to name,
                        "openHour" to open,
                        "closeHour" to close
                    )
                    db.collection("facilities").add(facility)
                        .addOnSuccessListener {
                            name = ""
                            openHour = "8"
                            closeHour = "20"
                            error = null
                        }
                        .addOnFailureListener { error = it.message }
                },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text("➕ Aggiungi campo")
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // Lista dei campi già presenti
            Text("Campi esistenti", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                items(facilities, key = { it.id }) { facility ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Nome: ${facility.name}")
                            Text("Orari: ${facility.openHour}:00 - ${facility.closeHour}:00")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = {
                                nav.navigate("editFacility/${facility.id}")
                            }) {
                                Text("Modifica orari")
                            }
                        }
                    }
                }
            }
        }
    }
}
