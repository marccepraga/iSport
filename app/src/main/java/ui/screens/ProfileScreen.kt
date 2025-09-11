package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(userId: String, nav: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var name by remember { mutableStateOf("...") }
    var isAdmin by remember { mutableStateOf(false) }
    var residence by remember { mutableStateOf("...") }
    var loading by remember { mutableStateOf(true) }

    // ✅ Controlla se userId è valido
    val safeUserId = if (userId.isNotBlank()) userId else auth.currentUser?.uid

    LaunchedEffect(safeUserId) {
        if (safeUserId.isNullOrBlank()) {
            // Nessun utente → torna al login
            nav.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            db.collection("users").document(safeUserId).get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: "Sconosciuto"
                    isAdmin = doc.getBoolean("admin") ?: false
                    residence = doc.getString("residence") ?: "Non specificato"
                    loading = false
                }
                .addOnFailureListener {
                    name = "Errore nel caricamento"
                    loading = false
                }
        }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Profilo utente", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        if (loading) {
            Text("Caricamento dati...")
        } else {
            Text("Nome: $name")
            Text("Ruolo: ${if (isAdmin) "Admin" else "Utente"}")
            Text("Residenza: $residence")
        }

        Spacer(Modifier.height(32.dp))

        Button(onClick = {
            auth.signOut()
            nav.navigate("login") {
                popUpTo(0) { inclusive = true } // 🔁 reset stack
            }
        }) {
            Text("Logout")
        }
    }
}
