package com.example.isport.model

import com.google.firebase.Timestamp

data class Booking(
    val id: String = "",
    val facilityId: String = "",        // riferimento al campo sportivo
    val userId: String = "",            // chi ha fatto la prenotazione
    val date: Timestamp? = null,        // giorno e ora della prenotazione
    val durationHours: Int = 1          // durata (es: 1 ora)
)
