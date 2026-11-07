package com.example.smartdoorapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResumenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val dbRef = FirebaseDatabase.getInstance().reference

    private lateinit var tvBienvenida: TextView
    private lateinit var tvEstadoHumo: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var tvEstadoPuerta: TextView
    private lateinit var btnAccionPuerta: Button
    private lateinit var btnCerrarSesion: Button

    private var estadoPuertaActual: Boolean = false // false = Bloqueada, true = Desbloqueada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen)

        auth = FirebaseAuth.getInstance()

        // Inicialización de vistas
        tvBienvenida = findViewById(R.id.tvBienvenida)
        tvEstadoHumo = findViewById(R.id.tvEstadoHumo)
        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvEstadoPuerta = findViewById(R.id.tvEstadoPuerta)
        btnAccionPuerta = findViewById(R.id.btnAccionPuerta)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        // Mostrar correo del usuario logueado
        val usuarioActual = auth.currentUser
        tvBienvenida.text = "Hola, ${usuarioActual?.email ?: "Usuario"}"

        // Escuchar datos de sensores e IoT en tiempo real
        escucharLecturasIoT()

        // Acción de Bloqueo/Desbloqueo de Puerta
        btnAccionPuerta.setOnClickListener {
            val nuevoEstado = !estadoPuertaActual
            dbRef.child("Puerta").child("desbloqueada").setValue(nuevoEstado)
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado de puerta actualizado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al comunicar con la puerta", Toast.LENGTH_SHORT).show()
                }
        }

        // Cierre de sesión
        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun escucharLecturasIoT() {
        // 1. Escuchar estado del sistema de incendios / sensores
        dbRef.child("Sensores").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val humoDetectado = snapshot.child("humo").getValue(Boolean::class.java) ?: false
                    val temperatura = snapshot.child("temperatura").getValue(Double::class.java) ?: 0.0

                    // Actualizar Temperatura
                    tvTemperatura.text = "$temperatura °C"

                    // Actualizar Alerta de Humo
                    if (humoDetectado) {
                        tvEstadoHumo.text = "¡ALERTA DE INCENDIO!"
                        tvEstadoHumo.setTextColor(Color.RED)
                    } else {
                        tvEstadoHumo.text = "Normal"
                        tvEstadoHumo.setTextColor(Color.parseColor("#388E3C"))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ResumenActivity, "Error al leer datos IoT: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // 2. Escuchar estado de la puerta
        dbRef.child("Puerta").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    estadoPuertaActual = snapshot.child("desbloqueada").getValue(Boolean::class.java) ?: false

                    if (estadoPuertaActual) {
                        tvEstadoPuerta.text = "Desbloqueada"
                        tvEstadoPuerta.setTextColor(Color.parseColor("#388E3C"))
                        btnAccionPuerta.text = "Bloquear Puerta"
                    } else {
                        tvEstadoPuerta.text = "Bloqueada"
                        tvEstadoPuerta.setTextColor(Color.RED)
                        btnAccionPuerta.text = "Desbloquear Puerta"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error opcional
            }
        })
    }
}