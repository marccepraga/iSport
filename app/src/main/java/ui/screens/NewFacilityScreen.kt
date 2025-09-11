package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isport.model.Facility
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun NewFacilityScreen(nav: NavController) {
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var comune by remember { mutableStateOf("") } // 👈 Campo nuovo
    var notes by remember { mutableStateOf("") }
    var openHour by remember { mutableStateOf(8) }
    var closeHour by remember { mutableStateOf(20) }
    val allDays = listOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")
    var selectedDays by remember { mutableStateOf<List<String>>(allDays) }

    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Nuovo campo sportivo", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome campo") })
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = sport, onValueChange = { sport = it }, label = { Text("Sport") })
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Indirizzo") })
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = comune, onValueChange = { comune = it }, label = { Text("Comune") }) // 👈 Campo nuovo
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Note (opzionale)") })
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = openHour.toString(),
            onValueChange = { openHour = it.toIntOrNull() ?: 0 },
            label = { Text("Ora apertura") }
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = closeHour.toString(),
            onValueChange = { closeHour = it.toIntOrNull() ?: 0 },
            label = { Text("Ora chiusura") }
        )
        Spacer(Modifier.height(16.dp))

        Text("Giorni di apertura:")
        allDays.forEach { day ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = day in selectedDays,
                    onCheckedChange = {
                        selectedDays = if (it) selectedDays + day else selectedDays - day
                    }
                )
                Text(day)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (success) {
            Text("Campo creato con successo!", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        Button(onClick = {
            if (name.isBlank() || sport.isBlank() || address.isBlank() || comune.isBlank()) {
                error = "Compila tutti i campi obbligatori"
                return@Button
            }

            val newFacility = Facility(
                name = name,
                sport = sport,
                address = address,
                comune = comune, // 👈 Salva il comune nel modello
                notes = notes,
                openHour = openHour,
                closeHour = closeHour,
                openDays = selectedDays
            )

            db.collection("facilities").add(newFacility)
                .addOnSuccessListener {
                    success = true
                    error = null
                    nav.popBackStack()
                }
                .addOnFailureListener {
                    error = "Errore creazione campo: ${it.message}"
                    success = false
                }
        }) {
            Text("Crea campo")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick = { nav.popBackStack() }) {
            Text("Annulla")
        }
    }
}
