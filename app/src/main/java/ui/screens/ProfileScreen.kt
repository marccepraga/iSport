package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(userId: String) {
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf<String?>(null) }
    var isResident by remember { mutableStateOf<Boolean?>(null) }
    var isAdmin by remember { mutableStateOf<Boolean?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                name = doc.getString("name")
                isResident = doc.getBoolean("resident")
                isAdmin = doc.getBoolean("admin")
            }
            .addOnFailureListener {
                error = it.message
            }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Profilo utente", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        when {
            error != null -> Text("❌ Errore: $error", color = MaterialTheme.colorScheme.error)
            name == null -> Text("🔄 Caricamento...")
            else -> {
                Text("Nome: $name")
                Spacer(Modifier.height(4.dp))
                Text("Residente: ${if (isResident == true) "Sì" else "No"}")
                Text("Ruolo: ${if (isAdmin == true) "Admin" else "Utente"}")
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
