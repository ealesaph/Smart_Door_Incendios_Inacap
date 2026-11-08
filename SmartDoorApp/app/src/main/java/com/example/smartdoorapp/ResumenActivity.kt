package com.example.smartdoorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
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

class ResumenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val dbRef = FirebaseDatabase.getInstance().reference
    private lateinit var notificationHelper: NotificationHelper

    private lateinit var tvBienvenida: TextView
    private lateinit var tvEstadoHumo: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var tvEstadoPuerta: TextView
    private lateinit var btnAccionPuerta: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var bottomNav: BottomNavigationView

    private var estadoPuertaActual: Boolean = false
    private var alertaNotificada: Boolean = false

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
        tvEstadoHumo = findViewById(R.id.tvEstadoHumo)
        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvEstadoPuerta = findViewById(R.id.tvEstadoPuerta)
        btnAccionPuerta = findViewById(R.id.btnAccionPuerta)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

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
                    Toast.makeText(this, "Sección en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        val usuarioActual = auth.currentUser
        tvBienvenida.text = "Hola, ${usuarioActual?.email ?: "Usuario"}"

        escucharLecturasIoT()

        btnAccionPuerta.setOnClickListener {
            val nuevoEstado = !estadoPuertaActual
            dbRef.child("Puerta").child("desbloqueada").setValue(nuevoEstado)
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
                                "🔥 ¡ALERTA DE INCENDIO DETECTADA!",
                                "Humo o alta temperatura ($temperatura °C) registrada."
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