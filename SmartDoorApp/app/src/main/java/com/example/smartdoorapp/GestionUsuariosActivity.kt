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

class GestionUsuariosActivity : AppCompatActivity() {

    private lateinit var rvUsuarios: RecyclerView
    private lateinit var tvVolverResumen: TextView
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_usuarios)

        rvUsuarios = findViewById(R.id.rvUsuarios)
        rvUsuarios.layoutManager = LinearLayoutManager(this)
        tvVolverResumen = findViewById(R.id.tvVolverResumen)

        tvVolverResumen.setOnClickListener {
            finish()
        }

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

        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        dbRef.child("Usuarios").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<UserModel>()
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val uid = userSnapshot.key ?: ""
                        val nombre = userSnapshot.child("nombre").getValue(String::class.java) ?: ""
                        val rut = userSnapshot.child("rut").getValue(String::class.java) ?: ""
                        val email = userSnapshot.child("email").getValue(String::class.java) ?: ""
                        val rol = userSnapshot.child("rol").getValue(String::class.java) ?: "user"
                        val passHash = userSnapshot.child("passwordHash").getValue(String::class.java) ?: ""
                        val salt = userSnapshot.child("salt").getValue(String::class.java) ?: ""

                        lista.add(UserModel(uid, nombre, rut, email, rol, passHash, salt))
                    }
                }

                rvUsuarios.adapter = UsuariosAdapter(lista) { usuario ->
                    val nuevoRol = if (usuario.esAdmin()) "user" else "admin"
                    dbRef.child("Usuarios").child(usuario.uid).child("rol").setValue(nuevoRol)
                        .addOnSuccessListener {
                            // Registrar el cambio de rol en la Bitácora de Auditoría (/Historial)
                            AuditoriaHelper.registrarEvento(
                                evento = "Cambio de rol a $nuevoRol para ${usuario.email}",
                                tipo = "SEGURIDAD",
                                rutUsuario = usuario.rut
                            )
                            Toast.makeText(this@GestionUsuariosActivity, "Rol actualizado a $nuevoRol para ${usuario.email}", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this@GestionUsuariosActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GestionUsuariosActivity, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
            }
        })
    }
}