package com.example.isport.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isport.model.Facility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun FacilitiesScreen(nav: NavController? = null, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid
    var isAdmin by remember { mutableStateOf(false) }

    var facilities by remember { mutableStateOf<List<Facility>>(emptyList()) }

    // Recupera informazioni sull’utente e controlla se è un admin
    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    isAdmin = doc.getBoolean("admin") == true
                }
        }
    }

    // Listener che aggiorna in tempo reale la lista dei campi sportivi
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
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center // centra tutto il contenuto
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Campi Sportivi Disponibili", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(20.dp))

                // Se non ci sono campi mostra un messaggio, altrimenti la lista
                if (facilities.isEmpty()) {
                    Text("Nessun campo presente.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                    ) {
                        items(facilities, key = { it.id }) { f ->
                            FacilityCard(
                                f,
                                isAdmin = isAdmin,
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

                Spacer(Modifier.height(24.dp))

                // Pulsante di aggiunta visibile solo agli admin
                if (isAdmin) {
                    Button(
                        onClick = { nav?.navigate("new_facility") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aggiungi nuovo campo",
                            tint = MaterialTheme.colorScheme.onPrimary // colore bianco sul pulsante
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Aggiungi nuovo campo")
                    }
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
    // Card centrata con dettagli del campo
    Card(
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(f.name, style = MaterialTheme.typography.titleMedium)
            Text("${f.sport} — ${f.address}")
            if (!f.notes.isNullOrBlank()) {
                Text("Note: ${f.notes}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))

            // Pulsanti centrati
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
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
