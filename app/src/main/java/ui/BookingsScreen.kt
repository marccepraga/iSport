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

    DisposableEffect(Unit) {
        val reg: ListenerRegistration = db.collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
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

            if (bookings.isEmpty()) {
                Text("Nessuna prenotazione trovata")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bookings, key = { it.id }) { b ->
                        Card {
                            Column(Modifier.padding(16.dp)) {
                                Text("Campo: ${b.facilityId}")
                                Text("Durata: ${b.durationHours}h")
                                Text("Data: ${b.date?.toDate()}")
                            }
                        }
                    }
                }
            }
        }
    }
}
