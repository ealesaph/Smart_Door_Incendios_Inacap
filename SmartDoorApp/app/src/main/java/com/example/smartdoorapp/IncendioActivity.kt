package com.example.smartdoorapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class IncendioActivity : AppCompatActivity() {

    private val dbRef = FirebaseDatabase.getInstance().reference

    private lateinit var tvNivelTexto: TextView
    private lateinit var tvDetalleNivel: TextView
    private lateinit var barBaja: View
    private lateinit var barMedia: View
    private lateinit var barAlta: View
    private lateinit var btnAbrirPuerta: Button
    private lateinit var btnLlamarEmergencias: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incendio)

        tvNivelTexto = findViewById(R.id.tvNivelTexto)
        tvDetalleNivel = findViewById(R.id.tvDetalleNivel)
        barBaja = findViewById(R.id.barBaja)
        barMedia = findViewById(R.id.barMedia)
        barAlta = findViewById(R.id.barAlta)
        btnAbrirPuerta = findViewById(R.id.btnAbrirPuerta)
        btnLlamarEmergencias = findViewById(R.id.btnLlamarEmergencias)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_incendio

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_resumen -> {
                    startActivity(Intent(this, ResumenActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_puertas -> {
                    startActivity(Intent(this, PuertasActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_plano -> {
                    startActivity(Intent(this, PlanoActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_incendio -> true
                else -> false
            }
        }

        // Navegación hacia el modal de Credencial de Apertura
        btnAbrirPuerta.setOnClickListener {
            val intent = Intent(this, CredencialAperturaActivity::class.java)
            startActivity(intent)
        }

        btnLlamarEmergencias.setOnClickListener {
            Toast.makeText(this, "Demostración: no se realiza llamada real", Toast.LENGTH_LONG).show()
        }

        escucharNivelIncendio()
    }

    private fun escucharNivelIncendio() {
        dbRef.child("Sensores").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val humo = snapshot.child("humo").getValue(Boolean::class.java) ?: false
                    val temp = snapshot.child("temperatura").getValue(Double::class.java) ?: 0.0
                    actualizarGraficoNivel(humo, temp)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarGraficoNivel(humo: Boolean, temp: Double) {
        when {
            humo || temp >= 50.0 -> {
                tvNivelTexto.text = "Alto"
                tvNivelTexto.setTextColor(Color.parseColor("#D92D20"))
                tvDetalleNivel.text = "Intensidad de llama alta ($temp °C)"
                barBaja.setBackgroundColor(Color.parseColor("#E4E7EC"))
                barMedia.setBackgroundColor(Color.parseColor("#C4B5FD"))
                barAlta.setBackgroundColor(Color.parseColor("#D92D20"))
            }
            temp in 30.0..49.9 -> {
                tvNivelTexto.text = "Medio"
                tvNivelTexto.setTextColor(Color.parseColor("#F79009"))
                tvDetalleNivel.text = "Elevación moderada de temperatura ($temp °C)"
                barBaja.setBackgroundColor(Color.parseColor("#E4E7EC"))
                barMedia.setBackgroundColor(Color.parseColor("#F79009"))
                barAlta.setBackgroundColor(Color.parseColor("#E4E7EC"))
            }
            else -> {
                tvNivelTexto.text = "Baja"
                tvNivelTexto.setTextColor(Color.parseColor("#12B76A"))
                tvDetalleNivel.text = "Valores de temperatura normales ($temp °C)"
                barBaja.setBackgroundColor(Color.parseColor("#12B76A"))
                barMedia.setBackgroundColor(Color.parseColor("#E4E7EC"))
                barAlta.setBackgroundColor(Color.parseColor("#E4E7EC"))
            }
        }
    }
}