package com.example.isport.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isport.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(nav: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    var isRegistering by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (isRegistering) "Registrazione" else "Login",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(8.dp))

        if (isRegistering) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Conferma Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
        }

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                error = null
                loading = true

                if (isRegistering) {
                    if (name.isBlank()) {
                        error = "Inserisci il nome"
                        loading = false
                        return@Button
                    }
                    if (password != confirmPassword) {
                        error = "Le password non coincidono"
                        loading = false
                        return@Button
                    }

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { res ->
                            val uid = res.user?.uid ?: return@addOnSuccessListener
                            val user = User(
                                id = uid,
                                name = name,
                                email = email,
                                isResident = true
                            )

                            db.collection("users").document(uid).set(user)
                                .addOnSuccessListener {
                                    nav.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    error = "Errore salvataggio utente: ${it.message}"
                                    loading = false
                                }
                        }
                        .addOnFailureListener {
                            error = "Errore registrazione: ${it.message}"
                            loading = false
                        }

                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            nav.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            isRegistering = true
                            error = "Utente non trovato, inserisci dati per registrarti"
                            loading = false
                        }
                }
            },
            enabled = !loading
        ) {
            Text(if (loading) "Attendi..." else if (isRegistering) "Registrati" else "Login")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = {
            isRegistering = !isRegistering
            error = null
        }) {
            Text(if (isRegistering) "Hai già un account? Accedi" else "Non hai un account? Registrati")
        }
    }
}
