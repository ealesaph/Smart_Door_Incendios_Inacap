package com.example.smartdoorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CredencialAperturaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credencial_apertura)

        auth = FirebaseAuth.getInstance()

        val tvVolverIncendio = findViewById<TextView>(R.id.tvVolverIncendio)
        val etUsuarioRut = findViewById<EditText>(R.id.etUsuarioRut)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        val btnValidarCredencial = findViewById<Button>(R.id.btnValidarCredencial)
        val tvCancelarSolicitud = findViewById<TextView>(R.id.tvCancelarSolicitud)
        val tvEstadoAutorizacion = findViewById<TextView>(R.id.tvEstadoAutorizacion)

        // Pre-cargar y fijar el usuario (correo) actualmente autenticado
        val currentUserEmail = auth.currentUser?.email ?: ""
        if (currentUserEmail.isNotEmpty()) {
            etUsuarioRut.setText(currentUserEmail)
            etUsuarioRut.isEnabled = false // Fijar el campo para que solo pida la contraseña
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_incendio

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_resumen -> {
                    startActivity(Intent(this, ResumenActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_incendio -> {
                    finish()
                    true
                }
                R.id.nav_puertas -> {
                    startActivity(Intent(this, PuertasActivity::class.java))
                    finish()
                    true
                }
                else -> {
                    Toast.makeText(this, "Sección en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }

        tvVolverIncendio.setOnClickListener { finish() }
        tvCancelarSolicitud.setOnClickListener { finish() }

        btnValidarCredencial.setOnClickListener {
            val email = auth.currentUser?.email ?: etUsuarioRut.text.toString().trim()
            val pass = etContrasena.text.toString().trim()

            if (pass.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu contraseña para confirmar la operación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar la contraseña contra Firebase Auth y verificar rol de Administrador
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: ""
                    dbRef.child("Usuarios").child(uid).get()
                        .addOnSuccessListener { snapshot ->
                            val rol = snapshot.child("rol").getValue(String::class.java) ?: "user"
                            val rutUser = snapshot.child("rut").getValue(String::class.java) ?: "N/A"

                            if (rol.lowercase() == "admin") {
                                // Consultar estado actual de la puerta para alternarlo (abrir o cerrar)
                                dbRef.child("Puerta").child("desbloqueada").get()
                                    .addOnSuccessListener { puertaSnap ->
                                        val estadoActual = puertaSnap.getValue(Boolean::class.java) ?: false
                                        val nuevoEstado = !estadoActual

                                        dbRef.child("Puerta").child("desbloqueada").setValue(nuevoEstado)
                                            .addOnSuccessListener {
                                                val accionTexto = if (nuevoEstado) "Apertura autorizada mediante contraseña" else "Cierre autorizado mediante contraseña"
                                                tvEstadoAutorizacion.text = "¡Autorización concedida! Puerta ${if (nuevoEstado) "Desbloqueada" else "Bloqueada"}."

                                                // Registrar evento en la Bitácora (/Historial)
                                                AuditoriaHelper.registrarEvento(
                                                    evento = accionTexto,
                                                    tipo = "SEGURIDAD",
                                                    rutUsuario = rutUser
                                                )

                                                Toast.makeText(this, "Operación autorizada con éxito", Toast.LENGTH_LONG).show()
                                                finish()
                                            }
                                    }
                            } else {
                                AuditoriaHelper.registrarEvento(
                                    evento = "Intento denegado de operar puerta ($email no es Admin)",
                                    tipo = "SEGURIDAD",
                                    rutUsuario = rutUser
                                )
                                tvEstadoAutorizacion.text = "Acceso denegado: Se requieren privilegios de Administrador."
                                Toast.makeText(this, "Permiso denegado: Se requieren privilegios de Administrador", Toast.LENGTH_LONG).show()
                            }
                        }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Contraseña incorrecta: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
