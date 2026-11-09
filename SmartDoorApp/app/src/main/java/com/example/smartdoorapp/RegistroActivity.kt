package com.example.smartdoorapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etRegistroEmail)
        val etRut = findViewById<EditText>(R.id.etRegistroRut)
        val etNombre = findViewById<EditText>(R.id.etRegistroNombre)
        val etPassword = findViewById<EditText>(R.id.etRegistroPassword)
        val rbAdmin = findViewById<RadioButton>(R.id.rbAdmin)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        val tvVolverLogin = findViewById<TextView>(R.id.tvVolverLogin)

        btnCrearCuenta.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val rut = etRut.text.toString().trim()
            val nombre = etNombre.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validaciones básicas de campos vacíos
            if (email.isEmpty() || rut.isEmpty() || nombre.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Determinar rol según la opción seleccionada en el formulario (o si el email contiene admin)
            val rolAsignado = if (rbAdmin.isChecked || email.lowercase().contains("admin")) "admin" else "user"

            // Crear usuario en Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid

                        if (uid != null) {
                            // Generar Salt único y Hash SHA-256 de la contraseña para almacenar en BD
                            val saltUnico = HashUtils.generarSalt()
                            val hashPassword = HashUtils.hashPassword(password, saltUnico)

                            val userMap = mapOf(
                                "rut" to rut,
                                "nombre" to nombre,
                                "email" to email,
                                "rol" to rolAsignado,
                                "passwordHash" to hashPassword,
                                "salt" to saltUnico
                            )

                            // Guardar perfil seguro en /Usuarios/{uid}
                            FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
                                .setValue(userMap)
                                .addOnSuccessListener {
                                    // Registrar en el Historial de Auditoría
                                    AuditoriaHelper.registrarEvento(
                                        evento = "Nuevo usuario registrado ($nombre)",
                                        tipo = "INFO",
                                        rutUsuario = rut
                                    )
                                }
                        }

                        Toast.makeText(this, "¡Cuenta creada exitosamente como $rolAsignado!", Toast.LENGTH_SHORT).show()

                        // Volver a la pantalla de Login
                        finish()
                    } else {
                        Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Evento para volver al Login sin registrarse
        tvVolverLogin.setOnClickListener {
            finish()
        }
    }
}
