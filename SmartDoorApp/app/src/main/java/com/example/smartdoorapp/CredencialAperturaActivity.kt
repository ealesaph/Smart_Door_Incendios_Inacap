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
            val email = etUsuarioRut.text.toString().trim()
            val pass = etContrasena.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu credencial completa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar credencial contra Firebase Auth
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    tvEstadoAutorizacion.text = "¡Autorización concedida! Desbloqueando puerta..."

                    // Orden de apertura de emergencia en Firebase Realtime Database
                    dbRef.child("Puerta").child("desbloqueada").setValue(true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Puerta autorizada y abierta con éxito", Toast.LENGTH_LONG).show()
                            finish()
                        }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Error de credencial: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}