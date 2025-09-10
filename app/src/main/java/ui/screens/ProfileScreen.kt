package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(userId: String) {
    Column(Modifier.padding(16.dp)) {
        Text("Profilo utente", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("User ID: $userId")
        Text("Residente: sì") // 👉 in futuro lo prendiamo da Firestore
        Text("Ruolo: utente")  // 👉 se admin mostriamo "admin"
    }
}
