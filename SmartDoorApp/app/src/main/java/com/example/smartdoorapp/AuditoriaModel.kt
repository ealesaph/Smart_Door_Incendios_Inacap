package com.example.smartdoorapp

data class AuditoriaModel(
    val id: String = "",
    val evento: String = "",
    val tipo: String = "INFO", // "SEGURIDAD", "EMERGENCIA", "INFO"
    val usuario: String = "",
    val rut: String = "",
    val timestamp: Long = 0L,
    val fecha: String = ""
)