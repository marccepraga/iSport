package com.example.isport.model

data class Facility(
    val id: String = "",
    val municipalityId: String = "",
    val name: String = "",
    val sport: String = "",    // "TENNIS", "CALCIO", "BASKET"
    val address: String = "",
    val notes: String? = null
)
