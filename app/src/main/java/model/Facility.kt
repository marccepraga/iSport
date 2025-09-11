package com.example.isport.model

data class Facility(
    val id: String = "",
    val name: String = "",
    val sport: String = "",
    val address: String = "",
    val comune: String = "",
    val notes: String = "",
    val openHour: Int = 8,
    val closeHour: Int = 20,
    val openDays: List<String> = emptyList()
)

