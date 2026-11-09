package com.example.smartdoorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PuertasActivity : AppCompatActivity() {

    private lateinit var rvPuertas: RecyclerView
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puertas)

        rvPuertas = findViewById(R.id.rvPuertas)
        rvPuertas.layoutManager = LinearLayoutManager(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_puertas

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_resumen -> {
                    startActivity(Intent(this, ResumenActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_incendio -> {
                    startActivity(Intent(this, IncendioActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_puertas -> true
                R.id.nav_plano -> {
                    startActivity(Intent(this, PlanoActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        cargarPuertas()
    }

    private fun cargarPuertas() {
        // Escuchamos el estado principal de la puerta en Firebase para actualizar la lista en tiempo real
        dbRef.child("Puerta").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val puertaPrincipalDesbloqueada = snapshot.child("desbloqueada").getValue(Boolean::class.java) ?: false
                val estadoPuertaPrincipal = if (puertaPrincipalDesbloqueada) "Abierta" else "Cerrada"
                val alertaPuertaPrincipal = if (puertaPrincipalDesbloqueada) "Alerta de cierre" else "Sin alertas"

                val lista = listOf(
                    PuertaModel("1", "P2-04", "Escalera A", estadoPuertaPrincipal, true, alertaPuertaPrincipal),
                    PuertaModel("2", "P2-06", "Archivo", "Sin datos", false, "Sin telemetría"),
                    PuertaModel("3", "P2-01", "Oficinas A", "Cerrada", true, "Sin alertas"),
                    PuertaModel("4", "P2-02", "Sala técnica", "Cerrada", true, "Sin alertas"),
                    PuertaModel("5", "P2-03", "Escalera B", "Cerrada", true, "Sin alertas"),
                    PuertaModel("6", "P2-05", "Oficinas B", "Cerrada", true, "Sin alertas")
                )

                rvPuertas.adapter = PuertasAdapter(lista) { puerta ->
                    if (puerta.id == "1") {
                        // Al hacer clic en P2-04 abre credencial para operar
                        startActivity(Intent(this@PuertasActivity, CredencialAperturaActivity::class.java))
                    } else {
                        Toast.makeText(this@PuertasActivity, "Puerta: ${puerta.codigo}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}