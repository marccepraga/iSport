package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.isport.model.Facility
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestoreSettings


@Composable
fun FacilitiesScreen() {
    val db = remember { FirebaseFirestore.getInstance() }
    val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(false) // 🔥 niente cache
        .build()
    db.firestoreSettings = settings

    var facilities by remember { mutableStateOf<List<Facility>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // 📡 Listener realtime su Firestore (si aggiorna automaticamente)
    DisposableEffect(Unit) {
        val reg: ListenerRegistration = db.collection("facilities")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    errorMsg = e.message
                    println("🔥 ERRORE Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                println("🔥 Documenti trovati: ${snap?.size() ?: 0}")
                snap?.forEach { doc ->
                    println("🔥 DOC ID = ${doc.id}, DATA = ${doc.data}")
                }

                // 🔎 Mapping manuale → non rischia errori
                facilities = snap?.documents?.map { doc ->
                    Facility(
                        id = doc.id,
                        municipalityId = doc.getString("municipalityId") ?: "",
                        name = doc.getString("name") ?: "",
                        sport = doc.getString("sport") ?: "",
                        address = doc.getString("address") ?: "",
                        notes = doc.getString("notes")
                    )
                } ?: emptyList()
            }
        onDispose { reg.remove() }
    }

    // 👇 GUI
    Scaffold { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Campi sportivi disponibili", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            if (errorMsg != null) {
                Text("Errore: $errorMsg", color = MaterialTheme.colorScheme.error)
            }

            if (facilities.isEmpty()) {
                Text("Nessun campo presente. Aggiungilo dalla console Firebase.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(facilities, key = { it.id }) { f ->
                        FacilityCard(f)
                    }
                }
            }
        }
    }
}

@Composable
private fun FacilityCard(f: Facility) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(f.name, style = MaterialTheme.typography.titleMedium)
            Text("${f.sport} — ${f.address}")
            if (!f.notes.isNullOrBlank()) {
                Text("Note: ${f.notes}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
