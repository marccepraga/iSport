package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    // 📡 Recupera campi in tempo reale
    LaunchedEffect(true) {
        db.collection("facilities")
            .addSnapshotListener { snap, _ ->
                facilities = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Facility::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Pannello Admin", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        // ➕ Aggiungi nuovo campo
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome campo") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = openHour, onValueChange = { openHour = it }, label = { Text("Ora apertura (es. 8)") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = closeHour, onValueChange = { closeHour = it }, label = { Text("Ora chiusura (es. 20)") })
        Spacer(Modifier.height(8.dp))

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(onClick = {
            val open = openHour.toIntOrNull()
            val close = closeHour.toIntOrNull()
            if (name.isBlank() || open == null || close == null || open >= close) {
                error = "Dati non validi"
                return@Button
            }

            val facility = mapOf("name" to name, "openHour" to open, "closeHour" to close)
            db.collection("facilities").add(facility)
                .addOnSuccessListener {
                    name = ""; openHour = "8"; closeHour = "20"; error = null
                }
                .addOnFailureListener { error = it.message }
        }) {
            Text("Aggiungi campo")
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        Text("Campi esistenti", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(facilities, key = { it.id }) { facility ->
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text("Nome: ${facility.name}")
                        Text("Orari: ${facility.openHour}:00 - ${facility.closeHour}:00")
                        Spacer(Modifier.height(8.dp))
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
