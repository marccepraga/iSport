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

    Column(Modifier.padding(16.dp)) {
        Text("Prenota $facilityId")
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = ore,
            onValueChange = { ore = it },
            label = { Text("Durata (ore)") }
        )
        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            val booking = Booking(
                userId = userId,
                facilityId = facilityId,
                date = Timestamp(Date()),
                durationHours = ore.toInt()
            )
            db.collection("bookings").add(booking)
                .addOnSuccessListener { onDone() }
        }) {
            Text("Conferma prenotazione")
        }
    }
}
