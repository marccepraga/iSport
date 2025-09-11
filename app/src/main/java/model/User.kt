package com.example.isport.model

data class User(
    val id: String = "",              // UID di Firebase Auth
    val name: String = "",
    val email: String = "",
    val isResident: Boolean = false,  // Solo i residenti possono prenotare
    val isAdmin: Boolean = false      // Per gestione admin in futuro
)
