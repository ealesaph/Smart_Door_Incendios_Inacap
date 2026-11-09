package com.example.smartdoorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase

class DebugActivity : AppCompatActivity() {

    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        val tvVolverResumen = findViewById<TextView>(R.id.tvVolverResumen)
        val btnSimularLlamaBaja = findViewById<Button>(R.id.btnSimularLlamaBaja)
        val btnSimularLlamaMedia = findViewById<Button>(R.id.btnSimularLlamaMedia)
        val btnSimularLlamaAlta = findViewById<Button>(R.id.btnSimularLlamaAlta)
        val btnNormalizarSensores = findViewById<Button>(R.id.btnNormalizarSensores)
        val btnAbrirPuertaDebug = findViewById<Button>(R.id.btnAbrirPuertaDebug)
        val btnCerrarPuertaDebug = findViewById<Button>(R.id.btnCerrarPuertaDebug)
        val btnVerBitacoraDebug = findViewById<Button>(R.id.btnVerBitacoraDebug)
        val btnGestionUsuariosDebug = findViewById<Button>(R.id.btnGestionUsuariosDebug)

        tvVolverResumen.setOnClickListener { finish() }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_resumen

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_resumen -> {
                    finish()
                    true
                }
                R.id.nav_puertas -> {
                    startActivity(Intent(this, PuertasActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_incendio -> {
                    startActivity(Intent(this, IncendioActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_plano -> {
                    startActivity(Intent(this, PlanoActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // 1. Simular Llama Baja (Humo = false, Temp = 35°C)
        btnSimularLlamaBaja.setOnClickListener {
            val sensoresData = mapOf(
                "humo" to false,
                "temperatura" to 35.0,
                "nivel" to "Baja"
            )
            dbRef.child("Sensores").setValue(sensoresData).addOnSuccessListener {
                AuditoriaHelper.registrarEvento(
                    evento = "Simulación de Llama Baja (35°C) activada",
                    tipo = "INFO",
                    rutUsuario = "DEBUG"
                )
                Toast.makeText(this, "Llama Baja simulada (35°C)", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Simular Llama Media (Humo = true, Temp = 45°C)
        btnSimularLlamaMedia.setOnClickListener {
            val sensoresData = mapOf(
                "humo" to true,
                "temperatura" to 45.0,
                "nivel" to "Media"
            )
            dbRef.child("Sensores").setValue(sensoresData).addOnSuccessListener {
                AuditoriaHelper.registrarEvento(
                    evento = "Simulación de Llama Media (45°C + Humo) activada",
                    tipo = "EMERGENCIA",
                    rutUsuario = "DEBUG"
                )
                Toast.makeText(this, "Llama Media simulada (45°C + Humo)", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Simular Llama Alta (Humo = true, Temp = 65°C - Crítico)
        btnSimularLlamaAlta.setOnClickListener {
            val sensoresData = mapOf(
                "humo" to true,
                "temperatura" to 65.0,
                "nivel" to "Alta"
            )
            dbRef.child("Sensores").setValue(sensoresData).addOnSuccessListener {
                AuditoriaHelper.registrarEvento(
                    evento = "Simulación de Llama Alta (65°C + Emergencia) activada",
                    tipo = "EMERGENCIA",
                    rutUsuario = "DEBUG"
                )
                Toast.makeText(this, "Llama Alta simulada (65°C Crítico)", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Normalizar Sensores (Humo = false, Temperatura = 22°C)
        btnNormalizarSensores.setOnClickListener {
            val sensoresData = mapOf(
                "humo" to false,
                "temperatura" to 22.0
            )
            dbRef.child("Sensores").setValue(sensoresData).addOnSuccessListener {
                AuditoriaHelper.registrarEvento(
                    evento = "Sensores restablecidos a valores normales (Debug)",
                    tipo = "INFO",
                    rutUsuario = "DEBUG"
                )
                Toast.makeText(this, "Sensores restablecidos a estado Normal", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Desbloquear Puerta
        btnAbrirPuertaDebug.setOnClickListener {
            dbRef.child("Puerta").child("desbloqueada").setValue(true).addOnSuccessListener {
                AuditoriaHelper.registrarEvento(
                    evento = "Desbloqueo de puerta simulado (Debug)",
                    tipo = "SEGURIDAD",
                    rutUsuario = "DEBUG"
                )
                Toast.makeText(this, "Puerta desbloqueada desde Debug", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Bloquear Puerta
        btnCerrarPuertaDebug.setOnClickListener {
            dbRef.child("Puerta").child("desbloqueada").setValue(false).addOnSuccessListener {
                AuditoriaHelper.registrarEvento(
                    evento = "Bloqueo de puerta simulado (Debug)",
                    tipo = "SEGURIDAD",
                    rutUsuario = "DEBUG"
                )
                Toast.makeText(this, "Puerta bloqueada desde Debug", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Ver Bitácora de Auditoría
        btnVerBitacoraDebug.setOnClickListener {
            startActivity(Intent(this, AuditoriaActivity::class.java))
        }

        // 6. Ver Panel de Usuarios
        btnGestionUsuariosDebug.setOnClickListener {
            startActivity(Intent(this, GestionUsuariosActivity::class.java))
        }
    }
}
