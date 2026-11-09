package com.example.smartdoorapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AuditoriaHelper {

    private val dbRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun registrarEvento(
        evento: String,
        tipo: String = "INFO",
        rutUsuario: String = "N/A"
    ) {
        val userEmail = auth.currentUser?.email ?: "Sistema"
        val timestamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fechaFormateada = sdf.format(Date(timestamp))

        val key = dbRef.child("Historial").push().key ?: return

        val historialItem = mapOf(
            "id" to key,
            "evento" to evento,
            "tipo" to tipo,
            "usuario" to userEmail,
            "rut" to rutUsuario,
            "timestamp" to timestamp,
            "fecha" to fechaFormateada
        )

        dbRef.child("Historial").child(key).setValue(historialItem)
    }
}
