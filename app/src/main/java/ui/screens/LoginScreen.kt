package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(nav: NavController) {
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    // Layout principale centrato nello schermo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))

            // Campi di input per email e password
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(16.dp))

            // Mostra eventuali errori di login
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            // Pulsante login
            Button(
                onClick = {
                    loading = true
                    error = null

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            nav.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            error = "Email o password non validi"
                            loading = false
                        }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(if (loading) "Attendi..." else "Login")
            }

            Spacer(Modifier.height(16.dp))

            // Link per registrazione nuovo account
            TextButton(
                onClick = { nav.navigate("register") },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text("Non hai un account? Registrati")
            }
        }
    }
}
