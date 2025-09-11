package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AuthScreen(nav: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Benvenuto su iSport", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { nav.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Accedi")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { nav.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrati")
        }
    }
}
