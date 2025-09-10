package com.example.isport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.isport.model.Booking
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun BookingForm(userId: String, facilityId: String, onDone: () -> Unit) {
    val db = FirebaseFirestore.getInstance()

    var ore by remember { mutableStateOf("1") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {
        Text("Prenota $facilityId", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = ore,
            onValueChange = { ore = it },
            label = { Text("Durata (ore)") }
        )
        Spacer(Modifier.height(12.dp))

        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val durata = ore.toIntOrNull() ?: 0
                if (durata <= 0) {
                    errorMsg = "Durata non valida"
                    return@Button
                }

                loading = true
                val now = Date()
                val fine = Calendar.getInstance().apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, 7)
                }.time

                // 🔎 Controllo utente residente
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { userSnap ->
                        val residente = userSnap.getBoolean("isResident") ?: false
                        if (!residente) {
                            errorMsg = "Solo i residenti possono prenotare"
                            loading = false
                            return@addOnSuccessListener
                        }

                        // 🔎 Calcola ore già prenotate nella settimana
                        val startOfWeek = Calendar.getInstance().apply {
                            time = now
                            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time

                        db.collection("bookings")
                            .whereEqualTo("userId", userId)
                            .whereGreaterThanOrEqualTo("date", Timestamp(startOfWeek))
                            .get()
                            .addOnSuccessListener { snap ->
                                val totalOre = snap.documents.sumOf { it.getLong("durationHours")?.toInt() ?: 0 }
                                if (totalOre + durata > 3) {
                                    errorMsg = "Massimo 3 ore prenotabili a settimana"
                                    loading = false
                                    return@addOnSuccessListener
                                }

                                // 🔎 Controlla che la prenotazione sia entro 7 giorni
                                val bookingDate = now // 👈 per semplicità prenota ora
                                if (bookingDate.after(fine)) {
                                    errorMsg = "Puoi prenotare solo entro 7 giorni"
                                    loading = false
                                    return@addOnSuccessListener
                                }

                                // ✅ Tutto ok → salva prenotazione
                                val booking = Booking(
                                    userId = userId,
                                    facilityId = facilityId,
                                    date = Timestamp(bookingDate),
                                    durationHours = durata
                                )

                                db.collection("bookings").add(booking)
                                    .addOnSuccessListener {
                                        println("🔥 Prenotazione salvata con id=${it.id}")
                                        loading = false
                                        onDone()
                                    }
                                    .addOnFailureListener { e ->
                                        println("❌ Errore salvataggio: ${e.message}")
                                        errorMsg = "Errore: ${e.message}"
                                        loading = false
                                    }
                            }
                            .addOnFailureListener { e ->
                                errorMsg = "Errore caricamento prenotazioni: ${e.message}"
                                loading = false
                            }
                    }
                    .addOnFailureListener { e ->
                        errorMsg = "Errore utente: ${e.message}"
                        loading = false
                    }
            },
            enabled = !loading
        ) {
            Text(if (loading) "Attendi..." else "Conferma prenotazione")
        }
    }
}
