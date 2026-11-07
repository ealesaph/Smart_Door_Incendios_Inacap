package com.example.smartdoorapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Llamamos al método de prueba al iniciar la app
        probarInsercionFirebase()

        val etUsuarioRut = findViewById<EditText>(R.id.etUsuarioRut)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        btnIngresar.setOnClickListener {
            val email = etUsuarioRut.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (email.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, contrasena)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    } // FIN de onCreate

    // FUNCIÓN DE PRUEBA: Debe ir FUERA de onCreate, pero DENTRO de MainActivity
    private fun probarInsercionFirebase() {
        val emailPrueba = "test.profesor@correo.cl"
        val passwordPrueba = "Prueba123456"
        val rutPrueba = "19.876.543-2"
        val nombrePrueba = "Usuario Test IoT"

        Log.d("FirebaseTest", "Iniciando prueba de inserción...")

        auth.createUserWithEmailAndPassword(emailPrueba, passwordPrueba)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    Log.d("FirebaseTest", "AUTH ÉXITO: Usuario creado con UID: $uid")

                    if (uid != null) {
                        val datosUsuario = mapOf(
                            "rut" to rutPrueba,
                            "nombre" to nombrePrueba,
                            "email" to emailPrueba,
                            "rol" to "Administrador",
                            "creadoEn" to System.currentTimeMillis()
                        )

                        FirebaseDatabase.getInstance()
                            .getReference("Usuarios")
                            .child(uid)
                            .setValue(datosUsuario)
                            .addOnSuccessListener {
                                Log.d("FirebaseTest", "DATABASE ÉXITO: Registro guardado")
                                Toast.makeText(this@MainActivity, "¡Prueba de inserción exitosa!", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { error ->
                                Log.e("FirebaseTest", "DATABASE ERROR: ${error.message}")
                            }
                    }
                } else {
                    Log.e("FirebaseTest", "AUTH ERROR: ${task.exception?.message}")
                }
            }
    }

} // FIN de la clase MainActivity