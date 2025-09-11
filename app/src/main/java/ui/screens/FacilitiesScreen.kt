package com.example.isport.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isport.model.Facility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun FacilitiesScreen(nav: NavController? = null, userId: String)
 {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid
    var isAdmin by remember { mutableStateOf(false) }

    var facilities by remember { mutableStateOf<List<Facility>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    isAdmin = doc.getBoolean("admin") == true
                }
        }
    }

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
                        FacilityCard(f, isAdmin = isAdmin,
                            onBook = {
                                nav?.navigate("bookingForm/${Uri.encode(f.id)}/${Uri.encode(f.name)}")
                            },
                            onEdit = {
                                nav?.navigate("edit_facility/${f.id}")
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ✅ Pulsante solo se admin
            if (isAdmin) {
                Button(onClick = {
                    nav?.navigate("new_facility")  // ← senza ID = crea nuovo
                }) {
                    Text("➕ Aggiungi nuovo campo")
                }
            }
        }
    }
}


@Composable
private fun FacilityCard(
    f: Facility,
    isAdmin: Boolean,
    onBook: () -> Unit,
    onEdit: () -> Unit
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(f.name, style = MaterialTheme.typography.titleMedium)
            Text("${f.sport} — ${f.address}")
            if (!f.notes.isNullOrBlank()) {
                Text("Note: ${f.notes}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onBook() }) {
                    Text("Prenota")
                }
                if (isAdmin) {
                    OutlinedButton(onClick = { onEdit() }) {
                        Text("Modifica")
                    }
                }
            }
        }
    }
}

