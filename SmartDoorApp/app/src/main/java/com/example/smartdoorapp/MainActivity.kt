package com.example.smartdoorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 1. Verificar si el usuario ya inició sesión previamente
        if (auth.currentUser != null) {
            irAHome()
            return
        }

        setContentView(R.layout.activity_login)

        // NOTA: Se comenta la función de prueba porque ya validaste la inserción en Firebase
        // probarInsercionFirebase()

        val etUsuarioRut = findViewById<EditText>(R.id.etUsuarioRut)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        // Acción del botón Ingresar
        btnIngresar.setOnClickListener {
            val email = etUsuarioRut.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (email.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Iniciar sesión con Firebase Auth
            auth.signInWithEmailAndPassword(email, contrasena)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                        irAHome()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Navegación hacia la pantalla de Registro
        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    // Función auxiliar para navegar a la pantalla de Resumen IoT
    private fun irAHome() {
        val intent = Intent(this, ResumenActivity::class.java)
        startActivity(intent)
        finish() // Cierra MainActivity para no volver al Login presionando el botón 'Atrás'
    }
}