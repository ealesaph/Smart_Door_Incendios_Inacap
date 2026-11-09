package com.example.smartdoorapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlanoActivity : AppCompatActivity() {

    private val dbRef = FirebaseDatabase.getInstance().reference

    private lateinit var tvEstadoCuadroP04: TextView
    private lateinit var tvEstadoPuertaSeleccionada: TextView
    private lateinit var btnVerDetallePuerta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plano)

        tvEstadoCuadroP04 = findViewById(R.id.tvEstadoCuadroP04)
        tvEstadoPuertaSeleccionada = findViewById(R.id.tvEstadoPuertaSeleccionada)
        btnVerDetallePuerta = findViewById(R.id.btnVerDetallePuerta)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_plano

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
                R.id.nav_incendio -> {
                    startActivity(Intent(this, IncendioActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_plano -> true
                else -> false
            }
        }

        btnVerDetallePuerta.setOnClickListener {
            // Lleva a la pantalla de credencial para operar o ver detalles de P2-04
            startActivity(Intent(this, CredencialAperturaActivity::class.java))
        }

        escucharEstadoPuertaEnPlano()
    }

    private fun escucharEstadoPuertaEnPlano() {
        dbRef.child("Puerta").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val desbloqueada = snapshot.child("desbloqueada").getValue(Boolean::class.java) ?: false

                    if (desbloqueada) {
                        tvEstadoCuadroP04.text = "04 Abierta"
                        tvEstadoCuadroP04.setTextColor(Color.parseColor("#B42318"))
                        tvEstadoCuadroP04.setBackgroundColor(Color.parseColor("#FEF3F2"))

                        tvEstadoPuertaSeleccionada.text = "Abierta"
                        tvEstadoPuertaSeleccionada.setTextColor(Color.parseColor("#B42318"))
                        tvEstadoPuertaSeleccionada.setBackgroundColor(Color.parseColor("#FEF3F2"))
                    } else {
                        tvEstadoCuadroP04.text = "04 Cerrada"
                        tvEstadoCuadroP04.setTextColor(Color.parseColor("#027A48"))
                        tvEstadoCuadroP04.setBackgroundColor(Color.parseColor("#EDFDF5"))

                        tvEstadoPuertaSeleccionada.text = "Cerrada"
                        tvEstadoPuertaSeleccionada.setTextColor(Color.parseColor("#027A48"))
                        tvEstadoPuertaSeleccionada.setBackgroundColor(Color.parseColor("#EDFDF5"))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}