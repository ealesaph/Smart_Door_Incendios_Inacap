package com.example.smartdoorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Redireccionar si el usuario ya inició sesión
        if (auth.currentUser != null) {
            startActivity(Intent(this, ResumenActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etUsuarioRut)
        val etPassword = findViewById<EditText>(R.id.etContrasena)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        btnIngresar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener

                    dbRef.child("Usuarios").child(uid).get().addOnSuccessListener { snapshot ->
                        val rutUser = snapshot.child("rut").getValue(String::class.java) ?: "N/A"

                        AuditoriaHelper.registrarEvento(
                            evento = "Inicio de sesión exitoso",
                            tipo = "SEGURIDAD",
                            rutUsuario = rutUser
                        )

                        Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ResumenActivity::class.java))
                        finish()
                    }.addOnFailureListener {
                        startActivity(Intent(this, ResumenActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { error ->
                    AuditoriaHelper.registrarEvento(
                        evento = "Intento fallido de inicio de sesión ($email)",
                        tipo = "SEGURIDAD",
                        rutUsuario = "N/A"
                    )

                    Toast.makeText(this, "Error de autenticación: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }

        tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }
}
