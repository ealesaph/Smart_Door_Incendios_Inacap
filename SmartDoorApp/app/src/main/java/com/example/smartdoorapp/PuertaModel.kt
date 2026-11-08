package com.example.smartdoorapp

data class PuertaModel(
    val id: String = "",
    val codigo: String = "",
    val ubicacion: String = "",
    val estado: String = "Cerrada", // "Abierta", "Cerrada", "Sin datos"
    val conectada: Boolean = true,
    val detalleAlerta: String = "Sin alertas"
)