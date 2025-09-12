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
import com.example.isport.model.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun BookingsScreen(userId: String, nav: NavController? = null) {
    val db = FirebaseFirestore.getInstance()
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Listener che osserva in tempo reale le prenotazioni dell'utente corrente
    DisposableEffect(Unit) {
        val reg: ListenerRegistration = db.collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    errorMsg = e.message
                    return@addSnapshotListener
                }
                bookings = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
        onDispose { reg.remove() }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Intestazione con titolo e pulsante per tornare alla lista dei campi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Le mie prenotazioni",
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(onClick = { nav?.navigate("facilities") }) {
                    Text("Torna ai campi")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Gestione dei tre stati: errore, lista vuota, lista con prenotazioni
            when {
                errorMsg != null -> {
                    Text(
                        "Errore: $errorMsg",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                bookings.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nessuna prenotazione trovata")
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(bookings, key = { it.id }) { b ->
                            BookingCard(
                                b,
                                onDelete = {
                                    db.collection("bookings").document(b.id).delete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(b: Booking, onDelete: () -> Unit) {
    // Card che mostra i dettagli della prenotazione
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Campo: ${b.facilityId}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text("Durata: ${b.durationHours}h")
            Text("Data: ${b.date?.toDate()}")

            Spacer(Modifier.height(12.dp))

            // Pulsante per eliminare la prenotazione corrente
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Cancella")
            }
        }
    }
}
