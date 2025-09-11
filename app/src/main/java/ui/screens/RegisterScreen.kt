package com.example.isport.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isport.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(nav: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var residence by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrazione", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = residence,
            onValueChange = { residence = it },
            label = { Text("Comune di residenza") },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Conferma Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(24.dp))

        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                errorMsg = null
                if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || residence.isBlank()) {
                    errorMsg = "Compila tutti i campi"
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMsg = "Le password non coincidono"
                    return@Button
                }

                loading = true

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val uid = authResult.user?.uid ?: return@addOnSuccessListener

                        val user = User(
                            id = uid, // 👈 uid salvato sia come campo
                            name = name,
                            email = email,
                            residence = residence,
                            isAdmin = false
                        )

                        // 👇 Salva con documentId = uid
                        db.collection("users").document(uid).set(user)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Registrazione completata", Toast.LENGTH_SHORT).show()
                                nav.navigate("main") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMsg = "Errore salvataggio utente: ${e.message}"
                                loading = false
                            }
                    }
                    .addOnFailureListener { e ->
                        errorMsg = "Errore registrazione: ${e.message}"
                        loading = false
                    }
            },
            enabled = !loading
        ) {
            Text(if (loading) "Attendi..." else "Registrati")
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = {
            nav.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
        }) {
            Text("Hai già un account? Accedi")
        }
    }
}
