package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditFacilityScreen(facilityId: String, nav: NavController) {
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var openHour by remember { mutableStateOf(8) }
    var closeHour by remember { mutableStateOf(20) }

    val allDays = listOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")
    var selectedDays by remember { mutableStateOf<List<String>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    // Caricamento iniziale dei dati della struttura da Firestore
    LaunchedEffect(facilityId) {
        db.collection("facilities").document(facilityId).get()
            .addOnSuccessListener { doc ->
                name = doc.getString("name") ?: ""
                openHour = doc.getLong("openHour")?.toInt() ?: 8
                closeHour = doc.getLong("closeHour")?.toInt() ?: 20
                selectedDays = doc.get("openDays") as? List<String> ?: emptyList()
                loading = false
            }
            .addOnFailureListener {
                error = "Errore caricamento: ${it.message}"
                loading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Modifica campo", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
            return@Column
        }

        // Intestazione con il nome del campo
        Text("Campo: $name")
        Spacer(Modifier.height(16.dp))

        // Campi di input per orario di apertura e chiusura
        OutlinedTextField(
            value = openHour.toString(),
            onValueChange = { openHour = it.toIntOrNull() ?: openHour },
            label = { Text("Ora di apertura") }
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = closeHour.toString(),
            onValueChange = { closeHour = it.toIntOrNull() ?: closeHour },
            label = { Text("Ora di chiusura") }
        )
        Spacer(Modifier.height(16.dp))

        // Giorni di apertura centrati
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Giorni di apertura")
            Spacer(Modifier.height(8.dp))

            allDays.forEach { day ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedDays.contains(day),
                        onCheckedChange = {
                            selectedDays = if (it) {
                                selectedDays + day
                            } else {
                                selectedDays - day
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(day)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Messaggi di errore o conferma salvataggio
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (success) {
            Text("Modifiche salvate con successo!", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        // Pulsanti affiancati
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (openHour >= closeHour) {
                        error = "L'orario di apertura deve essere inferiore a quello di chiusura"
                        return@Button
                    }

                    db.collection("facilities").document(facilityId)
                        .update(
                            mapOf(
                                "openHour" to openHour,
                                "closeHour" to closeHour,
                                "openDays" to selectedDays
                            )
                        )
                        .addOnSuccessListener {
                            success = true
                            error = null
                        }
                        .addOnFailureListener {
                            error = "Errore salvataggio: ${it.message}"
                            success = false
                        }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Salva modifiche")
            }

            OutlinedButton(
                onClick = { nav.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Torna indietro")
            }
        }
    }
}
