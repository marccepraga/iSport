package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.isport.model.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun BookingsScreen(userId: String) {
    val db = FirebaseFirestore.getInstance()
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // 📡 Listener realtime su Firestore
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
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Le mie prenotazioni", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            if (errorMsg != null) {
                Text("Errore: $errorMsg", color = MaterialTheme.colorScheme.error)
            }

            if (bookings.isEmpty()) {
                Text("Nessuna prenotazione trovata")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bookings, key = { it.id }) { b ->
                        BookingCard(b, onDelete = {
                            db.collection("bookings").document(b.id).delete()
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(b: Booking, onDelete: () -> Unit) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text("Campo: ${b.facilityId}", style = MaterialTheme.typography.titleMedium)
            Text("Durata: ${b.durationHours}h")
            Text("Data: ${b.date?.toDate()}")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onDelete() }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )) {
                Text("Cancella")
            }
        }
    }
}
