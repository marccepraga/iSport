package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isport.model.Facility

@Composable
fun FacilityDetailScreen(navController: NavController) {
    val facility = navController.previousBackStackEntry
        ?.savedStateHandle?.get<Facility>("facility")

    if (facility == null) {
        Text("Errore: campo non trovato.")
        return
    }

    Scaffold { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text(facility.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Sport: ${facility.sport}")
            Text("Indirizzo: ${facility.address}")
            if (!facility.notes.isNullOrBlank()) {
                Text("Note: ${facility.notes}")
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                navController.navigate("bookingForm/${facility.id}")
            }) {
                Text("Prenota")
            }

        }
    }
}
