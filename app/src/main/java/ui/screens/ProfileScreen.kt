package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth


@Composable
fun ProfileScreen(userId: String, nav: NavController) {
    val db = FirebaseFirestore.getInstance()
    var name by remember { mutableStateOf("...") }
    var isAdmin by remember { mutableStateOf(false) }
    var isResident by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                name = doc.getString("name") ?: "Sconosciuto"
                isAdmin = doc.getBoolean("admin") ?: false
                isResident = doc.getBoolean("resident") ?: false
                loading = false
            }
            .addOnFailureListener {
                name = "Errore nel caricamento"
                loading = false
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
            Text("Residente: ${if (isResident) "Sì" else "No"}")
        }

        Spacer(Modifier.height(32.dp))

        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            nav.navigate("login") {
                popUpTo(0) { inclusive = true } // 🔁 elimina tutto lo stack
            }
        }) {
            Text("Logout")
        }
    }
}

