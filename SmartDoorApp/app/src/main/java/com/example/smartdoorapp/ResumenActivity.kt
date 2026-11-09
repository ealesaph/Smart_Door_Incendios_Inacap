package com.example.smartdoorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResumenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val dbRef = FirebaseDatabase.getInstance().reference
    private lateinit var notificationHelper: NotificationHelper

    private lateinit var tvBienvenida: TextView
    private lateinit var tvRolUsuario: TextView
    private lateinit var tvEstadoHumo: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var tvEstadoPuerta: TextView
    private lateinit var btnAccionPuerta: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnGestionUsuarios: Button
    private lateinit var btnModoDebug: Button
    private lateinit var bottomNav: BottomNavigationView

    private var estadoPuertaActual: Boolean = false
    private var alertaNotificada: Boolean = false
    private var esAdminActual: Boolean = false
    private var rutUsuarioActual: String = "N/A"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Permiso denegado: No recibirás notificaciones de incendio", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen)

        auth = FirebaseAuth.getInstance()
        notificationHelper = NotificationHelper(this)

        verificarPermisoNotificaciones()

        tvBienvenida = findViewById(R.id.tvBienvenida)
        tvRolUsuario = findViewById(R.id.tvRolUsuario)
        tvEstadoHumo = findViewById(R.id.tvEstadoHumo)
        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvEstadoPuerta = findViewById(R.id.tvEstadoPuerta)
        btnAccionPuerta = findViewById(R.id.btnAccionPuerta)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios)
        btnModoDebug = findViewById(R.id.btnModoDebug)

        // Configuración de Navegación Inferior
        bottomNav = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_resumen

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_resumen -> true
                R.id.nav_incendio -> {
                    startActivity(Intent(this, IncendioActivity::class.java))
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
                else -> false
            }
        }

        val usuarioActual = auth.currentUser
        val emailUser = usuarioActual?.email ?: "Usuario"
        tvBienvenida.text = "Hola, $emailUser"

        // Escuchar datos del usuario en Firebase para validar Rol (Admin / User)
        cargarDatosUsuario()

        escucharLecturasIoT()

        btnAccionPuerta.setOnClickListener {
            if (!esAdminActual) {
                Toast.makeText(this, "Acceso denegado: Se requieren privilegios de Administrador", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val nuevoEstado = !estadoPuertaActual
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fechaActual = sdf.format(Date())
            val userEmail = auth.currentUser?.email ?: "desconocido@correo.cl"

            // Actualizar el nodo /Puerta con metadatos
            val mapaPuerta = mapOf(
                "desbloqueada" to nuevoEstado,
                "modificado_por" to userEmail,
                "ultima_modificacion" to fechaActual
            )

            dbRef.child("Puerta").updateChildren(mapaPuerta).addOnSuccessListener {
                // Registrar en la Bitácora de Auditoría (/Historial)
                val accionTexto = if (nuevoEstado) "Desbloqueo manual de puerta" else "Bloqueo manual de puerta"
                AuditoriaHelper.registrarEvento(
                    evento = accionTexto,
                    tipo = "SEGURIDAD",
                    rutUsuario = rutUsuarioActual
                )
            }
        }

        btnGestionUsuarios.setOnClickListener {
            startActivity(Intent(this, GestionUsuariosActivity::class.java))
        }

        btnModoDebug.setOnClickListener {
            startActivity(Intent(this, DebugActivity::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun verificarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return
        dbRef.child("Usuarios").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                    val rol = snapshot.child("rol").getValue(String::class.java) ?: "user"
                    rutUsuarioActual = snapshot.child("rut").getValue(String::class.java) ?: "N/A"
                    esAdminActual = rol.lowercase() == "admin"

                    if (nombre.isNotEmpty()) {
                        tvBienvenida.text = "Hola, $nombre"
                    }

                    if (esAdminActual) {
                        tvRolUsuario.text = "Rol: Administrador"
                        tvRolUsuario.setTextColor(Color.parseColor("#B42318"))
                        btnGestionUsuarios.visibility = View.VISIBLE
                    } else {
                        tvRolUsuario.text = "Rol: Usuario Estándar"
                        tvRolUsuario.setTextColor(Color.parseColor("#475467"))
                        btnGestionUsuarios.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun escucharLecturasIoT() {
        dbRef.child("Sensores").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val humoDetectado = snapshot.child("humo").getValue(Boolean::class.java) ?: false
                    val temperatura = snapshot.child("temperatura").getValue(Double::class.java) ?: 0.0

                    tvTemperatura.text = "$temperatura °C"

                    if (humoDetectado || temperatura > 45.0) {
                        tvEstadoHumo.text = "¡ALERTA DE INCENDIO!"
                        tvEstadoHumo.setTextColor(Color.parseColor("#D92D20"))

                        if (!alertaNotificada) {
                            notificationHelper.enviarAlertaIncendio(
                                "ALERTA DE INCENDIO DETECTADA",
                                "Humo o alta temperatura ($temperatura °C) registrada."
                            )

                            // Registrar la Alerta de Incendio en el Historial de Auditoría (/Historial)
                            AuditoriaHelper.registrarEvento(
                                evento = "Alerta de temperatura alta ($temperatura °C)",
                                tipo = "EMERGENCIA",
                                rutUsuario = "N/A"
                            )

                            alertaNotificada = true
                        }
                    } else {
                        tvEstadoHumo.text = "Normal"
                        tvEstadoHumo.setTextColor(Color.parseColor("#12B76A"))
                        alertaNotificada = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        dbRef.child("Puerta").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    estadoPuertaActual = snapshot.child("desbloqueada").getValue(Boolean::class.java) ?: false
                    if (estadoPuertaActual) {
                        tvEstadoPuerta.text = "Desbloqueada"
                        tvEstadoPuerta.setTextColor(Color.parseColor("#12B76A"))
                        btnAccionPuerta.text = "Bloquear Puerta"
                    } else {
                        tvEstadoPuerta.text = "Bloqueada"
                        tvEstadoPuerta.setTextColor(Color.parseColor("#D92D20"))
                        btnAccionPuerta.text = "Desbloquear Puerta"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}