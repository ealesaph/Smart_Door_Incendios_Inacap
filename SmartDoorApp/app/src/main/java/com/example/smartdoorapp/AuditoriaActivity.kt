package com.example.smartdoorapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AuditoriaActivity : AppCompatActivity() {

    private lateinit var rvAuditoria: RecyclerView
    private lateinit var tvVolverResumen: TextView
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auditoria)

        rvAuditoria = findViewById(R.id.rvAuditoria)
        rvAuditoria.layoutManager = LinearLayoutManager(this)
        tvVolverResumen = findViewById(R.id.tvVolverResumen)

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

        cargarHistorialAuditoria()
    }

    private fun cargarHistorialAuditoria() {
        dbRef.child("Historial").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<AuditoriaModel>()
                if (snapshot.exists()) {
                    for (itemSnapshot in snapshot.children) {
                        val id = itemSnapshot.key ?: ""
                        val evento = itemSnapshot.child("evento").getValue(String::class.java) ?: ""
                        val tipo = itemSnapshot.child("tipo").getValue(String::class.java) ?: "INFO"
                        val usuario = itemSnapshot.child("usuario").getValue(String::class.java) ?: ""
                        val rut = itemSnapshot.child("rut").getValue(String::class.java) ?: ""
                        val timestamp = itemSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        val fecha = itemSnapshot.child("fecha").getValue(String::class.java) ?: ""

                        lista.add(AuditoriaModel(id, evento, tipo, usuario, rut, timestamp, fecha))
                    }
                }

                // Ordenar para mostrar los registros más recientes arriba
                lista.sortByDescending { it.timestamp }
                rvAuditoria.adapter = AuditoriaAdapter(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AuditoriaActivity, "Error al cargar la bitácora", Toast.LENGTH_SHORT).show()
            }
        })
    }
}