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
import com.google.firebase.firestore.ListenerRegistration
import android.net.Uri


@Composable
fun FacilitiesScreen(nav: NavController? = null) {
    val db = FirebaseFirestore.getInstance()
    var facilities by remember { mutableStateOf<List<Facility>>(emptyList()) }

    DisposableEffect(Unit) {
        val reg: ListenerRegistration = db.collection("facilities")
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
                facilities = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Facility::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
        onDispose { reg.remove() }
    }

    Scaffold { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Campi sportivi disponibili", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            if (facilities.isEmpty()) {
                Text("Nessun campo presente.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(facilities, key = { it.id }) { f ->
                        FacilityCard(f) {
                            nav?.navigate("bookingForm/${Uri.encode(f.id)}/${Uri.encode(f.name)}")

                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FacilityCard(f: Facility, onBook: () -> Unit) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(f.name, style = MaterialTheme.typography.titleMedium)
            Text("${f.sport} — ${f.address}")
            if (!f.notes.isNullOrBlank()) {
                Text("Note: ${f.notes}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onBook() }) {
                Text("Prenota")
            }
        }
    }
}
