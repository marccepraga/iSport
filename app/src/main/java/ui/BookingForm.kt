package com.example.isport.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.isport.model.Booking
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

@Composable
fun BookingForm(userId: String, facilityId: String, facilityName: String, onDone: () -> Unit) {
    val db = FirebaseFirestore.getInstance()

    var openHour by remember { mutableStateOf(8) }
    var closeHour by remember { mutableStateOf(20) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var ore by remember { mutableStateOf("1") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    // 🔄 Leggi orari apertura dal DB
    LaunchedEffect(facilityId) {
        db.collection("facilities").document(facilityId).get()
            .addOnSuccessListener { doc ->
                openHour = doc.getLong("openHour")?.toInt() ?: 8
                closeHour = doc.getLong("closeHour")?.toInt() ?: 20
            }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Prenota $facilityName", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Text("Data selezionata: ${dateFormat.format(selectedDate)}")
        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            if (hour in openHour until closeHour) {
                                cal.set(year, month, day, hour, minute)
                                selectedDate = cal.time
                            } else {
                                errorMsg = "Orario non valido: il campo è aperto dalle $openHour alle $closeHour"
                            }
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text("Scegli data e ora")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = ore,
            onValueChange = { ore = it },
            label = { Text("Durata (ore)") }
        )

        Spacer(Modifier.height(16.dp))

        errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
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

                val bookingDate = selectedDate

                // 🔍 Controllo utente e comune
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { userSnap ->
                        val residence = userSnap.getString("residence") ?: ""
                        if (residence.isBlank()) {
                            errorMsg = "Comune di residenza non valido"
                            loading = false
                            return@addOnSuccessListener
                        }

                        db.collection("facilities").document(facilityId).get()
                            .addOnSuccessListener { facilitySnap ->
                                val comuneCampo = facilitySnap.getString("comune") ?: ""
                                if (comuneCampo.isBlank()) {
                                    errorMsg = "Il campo non ha un comune assegnato"
                                    loading = false
                                    return@addOnSuccessListener
                                }

                                if (residence != comuneCampo) {
                                    errorMsg = "Puoi prenotare solo campi nel tuo comune di residenza ($residence)"
                                    loading = false
                                    return@addOnSuccessListener
                                }

                                // ✅ Controllo ore settimanali
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

                                        if (bookingDate.after(fine)) {
                                            errorMsg = "Puoi prenotare solo entro 7 giorni"
                                            loading = false
                                            return@addOnSuccessListener
                                        }

                                        // 🔍 Controllo sovrapposizione prenotazioni
                                        db.collection("bookings")
                                            .whereEqualTo("facilityId", facilityId)
                                            .whereEqualTo("date", Timestamp(bookingDate))
                                            .get()
                                            .addOnSuccessListener { overlaps ->
                                                if (!overlaps.isEmpty) {
                                                    errorMsg = "Questo orario è già stato prenotato"
                                                    loading = false
                                                    return@addOnSuccessListener
                                                }

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
                                                        errorMsg = "Errore: ${e.message}"
                                                        loading = false
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMsg = "Errore controllo orari: ${e.message}"
                                                loading = false
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        errorMsg = "Errore controllo ore: ${e.message}"
                                        loading = false
                                    }
                            }
                            .addOnFailureListener { e ->
                                errorMsg = "Errore lettura campo: ${e.message}"
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
