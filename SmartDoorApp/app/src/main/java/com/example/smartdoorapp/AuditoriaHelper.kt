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
        val user = auth.currentUser
        val userEmail = user?.email ?: "Sistema"
        val uid = user?.uid
        val timestamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fechaFormateada = sdf.format(Date(timestamp))

        val key = dbRef.child("Historial").push().key ?: return

        if (rutUsuario == "N/A" && uid != null) {
            dbRef.child("Usuarios").child(uid).child("rut").get().addOnSuccessListener { snapshot ->
                val rutEncontrado = snapshot.getValue(String::class.java) ?: "N/A"
                guardarRegistro(key, evento, tipo, userEmail, rutEncontrado, timestamp, fechaFormateada)
            }.addOnFailureListener {
                guardarRegistro(key, evento, tipo, userEmail, rutUsuario, timestamp, fechaFormateada)
            }
        } else {
            guardarRegistro(key, evento, tipo, userEmail, rutUsuario, timestamp, fechaFormateada)
        }
    }

    private fun guardarRegistro(
        key: String,
        evento: String,
        tipo: String,
        userEmail: String,
        rutUsuario: String,
        timestamp: Long,
        fechaFormateada: String
    ) {
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
