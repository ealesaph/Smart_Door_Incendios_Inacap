package com.example.smartdoorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlanoActivity : AppCompatActivity() {

    private val dbRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val NUMERO_EMERGENCIA = "+56930374007"

    private lateinit var tvEstadoCuadroP01: TextView
    private lateinit var tvEstadoCuadroP02: TextView
    private lateinit var tvEstadoCuadroP03: TextView
    private lateinit var tvEstadoCuadroP04: TextView
    private lateinit var tvEstadoCuadroP05: TextView
    private lateinit var tvEstadoCuadroP06: TextView

    private lateinit var tvNombrePuertaSeleccionada: TextView
    private lateinit var tvEstadoPuertaSeleccionada: TextView
    private lateinit var btnAbrirPuertaPlano: Button
    private lateinit var btnCerrarPuertaPlano: Button
    private lateinit var btnVerDetallePuerta: Button
    private lateinit var btnLlamarEmergenciasPlano: Button
    private lateinit var cardAlertaCriticaPlano: View

    private var codigoSeleccionado: String = "P2-04"
    private var ubicacionSeleccionada: String = "Escalera A"
    private var esAdminActual: Boolean = false

    private var humoActual: Boolean = false
    private var temperaturaActual: Double = 22.0
    private var nivelFuegoActual: String = "Normal" // "Normal", "Baja", "Media", "Alta"

    private val estadosPuertasDemo = mutableMapOf(
        "P2-01" to false,
        "P2-02" to false,
        "P2-03" to false,
        "P2-04" to false,
        "P2-05" to false,
        "P2-06" to false
    )

    private val requestCallPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            realizarLlamadaDirecta()
        } else {
            Toast.makeText(this, "Permiso denegado. Abriendo marcador...", Toast.LENGTH_SHORT).show()
            abrirMarcadorLlamada()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plano)

        tvEstadoCuadroP01 = findViewById(R.id.tvEstadoCuadroP01)
        tvEstadoCuadroP02 = findViewById(R.id.tvEstadoCuadroP02)
        tvEstadoCuadroP03 = findViewById(R.id.tvEstadoCuadroP03)
        tvEstadoCuadroP04 = findViewById(R.id.tvEstadoCuadroP04)
        tvEstadoCuadroP05 = findViewById(R.id.tvEstadoCuadroP05)
        tvEstadoCuadroP06 = findViewById(R.id.tvEstadoCuadroP06)

        tvNombrePuertaSeleccionada = findViewById(R.id.tvNombrePuertaSeleccionada)
        tvEstadoPuertaSeleccionada = findViewById(R.id.tvEstadoPuertaSeleccionada)
        btnAbrirPuertaPlano = findViewById(R.id.btnAbrirPuertaPlano)
        btnCerrarPuertaPlano = findViewById(R.id.btnCerrarPuertaPlano)
        btnVerDetallePuerta = findViewById(R.id.btnVerDetallePuerta)
        btnLlamarEmergenciasPlano = findViewById(R.id.btnLlamarEmergenciasPlano)
        cardAlertaCriticaPlano = findViewById(R.id.cardAlertaCriticaPlano)

        val boxP01 = findViewById<View>(R.id.boxP01)
        val boxP02 = findViewById<View>(R.id.boxP02)
        val boxP03 = findViewById<View>(R.id.boxP03)
        val boxP04 = findViewById<View>(R.id.boxP04)
        val boxP05 = findViewById<View>(R.id.boxP05)
        val boxP06 = findViewById<View>(R.id.boxP06)

        val btnSimularPlanoBaja = findViewById<Button>(R.id.btnSimularPlanoBaja)
        val btnSimularPlanoMedia = findViewById<Button>(R.id.btnSimularPlanoMedia)
        val btnSimularPlanoAlta = findViewById<Button>(R.id.btnSimularPlanoAlta)
        val btnSimularPlanoNormal = findViewById<Button>(R.id.btnSimularPlanoNormal)

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

        verificarRolUsuario()

        boxP01.setOnClickListener { seleccionarPuerta("P2-01", "Oficinas A") }
        boxP02.setOnClickListener { seleccionarPuerta("P2-02", "Sala técnica") }
        boxP03.setOnClickListener { seleccionarPuerta("P2-03", "Escalera B") }
        boxP04.setOnClickListener { seleccionarPuerta("P2-04", "Escalera A") }
        boxP05.setOnClickListener { seleccionarPuerta("P2-05", "Oficinas B") }
        boxP06.setOnClickListener { seleccionarPuerta("P2-06", "Archivo") }

        // Botones de Simulación de Incendio en el Plano
        btnSimularPlanoBaja.setOnClickListener { simularLlamaPlano(false, 35.0, "Baja") }
        btnSimularPlanoMedia.setOnClickListener { simularLlamaPlano(true, 45.0, "Media") }
        btnSimularPlanoAlta.setOnClickListener { simularLlamaPlano(true, 65.0, "Alta") }
        btnSimularPlanoNormal.setOnClickListener { simularLlamaPlano(false, 22.0, "Normal") }

        btnAbrirPuertaPlano.setOnClickListener {
            solicitarAperturaPuerta()
        }

        btnCerrarPuertaPlano.setOnClickListener {
            ejecutarCambioEstadoPuerta(false)
        }

        btnVerDetallePuerta.setOnClickListener {
            startActivity(Intent(this, CredencialAperturaActivity::class.java))
        }

        btnLlamarEmergenciasPlano.setOnClickListener {
            iniciarLlamadaEmergencia()
        }

        escucharEstadoPuertaEnPlano()
        escucharSensoresEnPlano()
    }

    private fun verificarRolUsuario() {
        val uid = auth.currentUser?.uid ?: return
        dbRef.child("Usuarios").child(uid).child("rol").get().addOnSuccessListener { snapshot ->
            val rol = snapshot.getValue(String::class.java) ?: "user"
            esAdminActual = rol.lowercase() == "admin"
        }
    }

    private fun simularLlamaPlano(humo: Boolean, temp: Double, nivel: String) {
        val sensoresData = mapOf(
            "humo" to humo,
            "temperatura" to temp,
            "nivel" to nivel
        )
        dbRef.child("Sensores").setValue(sensoresData).addOnSuccessListener {
            AuditoriaHelper.registrarEvento(
                evento = "Simulación de Llama $nivel ($temp°C) desde Plano",
                tipo = if (nivel == "Normal") "INFO" else "EMERGENCIA"
            )
            Toast.makeText(this, "Nivel de Llama actualizado a $nivel ($temp °C)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun seleccionarPuerta(codigo: String, ubicacion: String) {
        codigoSeleccionado = codigo
        ubicacionSeleccionada = ubicacion
        tvNombrePuertaSeleccionada.text = "$codigo · $ubicacion"
        actualizarUISeleccionada()
    }

    private fun solicitarAperturaPuerta() {
        if (!esAdminActual) {
            Toast.makeText(this, "Permiso denegado: Se requieren privilegios de Administrador", Toast.LENGTH_LONG).show()
            return
        }

        // Evaluar nivel de riesgo según temperatura y estado de fuego
        when {
            temperaturaActual >= 50.0 || nivelFuegoActual.uppercase() == "ALTA" -> {
                // NIVEL ALTO: Doble Confirmación con Alerta de Riesgo Extremo
                mostrarConfirmacionNivelAltoPaso1()
            }
            temperaturaActual >= 40.0 || humoActual || nivelFuegoActual.uppercase() == "MEDIA" -> {
                // NIVEL MEDIO: Advertencia de posible descarrilamiento del incidente
                mostrarAdvertenciaNivelMedio()
            }
            else -> {
                // NIVEL BAJO / NORMAL: Operación estándar
                ejecutarCambioEstadoPuerta(true)
            }
        }
    }

    private fun mostrarAdvertenciaNivelMedio() {
        AlertDialog.Builder(this)
            .setTitle("ADVERTENCIA DE SEGURIDAD - NIVEL MEDIO")
            .setMessage("Abrir la puerta $codigoSeleccionado en Nivel Medio de Incendio ($temperaturaActual °C) puede desoxigenar o descarrilar la contención del incidente. ¿Desea proceder bajo su responsabilidad?")
            .setPositiveButton("Proceder y Abrir") { _, _ ->
                ejecutarCambioEstadoPuerta(true)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarConfirmacionNivelAltoPaso1() {
        AlertDialog.Builder(this)
            .setTitle("¡RIESGO EXTREMO DE INCENDIO - ALTA!")
            .setMessage("¡PELIGRO EXTREMO! La temperatura actual ($temperaturaActual °C) es muy alta. La apertura de la puerta $codigoSeleccionado representa un ALTO RIESGO de provocar propagación acelerada de llama o efecto Flashover. ¿Comprende el riesgo y desea continuar?")
            .setPositiveButton("Entiendo el Riesgo / Continuar") { _, _ ->
                mostrarConfirmaciónNivelAltoPaso2()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarConfirmaciónNivelAltoPaso2() {
        AlertDialog.Builder(this)
            .setTitle("CONFIRMACIÓN FINAL DE SEGURIDAD")
            .setMessage("CONFIRMACIÓN FINAL: ¿Autoriza de manera explícita la apertura de emergencia de la puerta $codigoSeleccionado bajo estado de RIESGO CRÍTICO?")
            .setPositiveButton("CONFIRMAR APERTURA BAJO RIESGO") { _, _ ->
                AuditoriaHelper.registrarEvento(
                    evento = "Apertura de puerta $codigoSeleccionado autorizada bajo RIESGO CRÍTICO ($temperaturaActual°C)",
                    tipo = "EMERGENCIA"
                )
                ejecutarCambioEstadoPuerta(true)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarCambioEstadoPuerta(abrir: Boolean) {
        if (!esAdminActual) {
            Toast.makeText(this, "Permiso denegado: Se requieren privilegios de Administrador", Toast.LENGTH_LONG).show()
            return
        }

        estadosPuertasDemo[codigoSeleccionado] = abrir
        if (codigoSeleccionado == "P2-04") {
            dbRef.child("Puerta").child("desbloqueada").setValue(abrir)
        } else {
            dbRef.child("PuertasDemo").child(codigoSeleccionado).child("desbloqueada").setValue(abrir)
        }

        val accionTexto = if (abrir) "Apertura de puerta $codigoSeleccionado desde Plano" else "Cierre de puerta $codigoSeleccionado desde Plano"
        AuditoriaHelper.registrarEvento(
            evento = accionTexto,
            tipo = "SEGURIDAD"
        )

        Toast.makeText(this, "Puerta $codigoSeleccionado ${if (abrir) "Desbloqueada" else "Bloqueada"}", Toast.LENGTH_SHORT).show()
        actualizarUISeleccionada()
        actualizarCuadrosPlano()
    }

    private fun actualizarUISeleccionada() {
        val estaAbierta = estadosPuertasDemo[codigoSeleccionado] ?: false
        if (estaAbierta) {
            tvEstadoPuertaSeleccionada.text = "Abierta"
            tvEstadoPuertaSeleccionada.setTextColor(Color.parseColor("#B42318"))
            tvEstadoPuertaSeleccionada.setBackgroundColor(Color.parseColor("#FEF3F2"))
        } else {
            tvEstadoPuertaSeleccionada.text = "Cerrada"
            tvEstadoPuertaSeleccionada.setTextColor(Color.parseColor("#027A48"))
            tvEstadoPuertaSeleccionada.setBackgroundColor(Color.parseColor("#EDFDF5"))
        }
    }

    private fun actualizarCuadrosPlano() {
        val p01 = estadosPuertasDemo["P2-01"] ?: false
        tvEstadoCuadroP01.text = if (p01) "01 Abierta" else "01 Cerrada"
        tvEstadoCuadroP01.setTextColor(Color.parseColor(if (p01) "#B42318" else "#027A48"))
        tvEstadoCuadroP01.setBackgroundColor(Color.parseColor(if (p01) "#FEF3F2" else "#EDFDF5"))

        val p02 = estadosPuertasDemo["P2-02"] ?: false
        tvEstadoCuadroP02.text = if (p02) "02 Abierta" else "02 Cerrada"
        tvEstadoCuadroP02.setTextColor(Color.parseColor(if (p02) "#B42318" else "#027A48"))
        tvEstadoCuadroP02.setBackgroundColor(Color.parseColor(if (p02) "#FEF3F2" else "#EDFDF5"))

        val p03 = estadosPuertasDemo["P2-03"] ?: false
        tvEstadoCuadroP03.text = if (p03) "03 Abierta" else "03 Cerrada"
        tvEstadoCuadroP03.setTextColor(Color.parseColor(if (p03) "#B42318" else "#027A48"))
        tvEstadoCuadroP03.setBackgroundColor(Color.parseColor(if (p03) "#FEF3F2" else "#EDFDF5"))

        val p04 = estadosPuertasDemo["P2-04"] ?: false
        tvEstadoCuadroP04.text = if (p04) "04 Abierta" else "04 Cerrada"
        tvEstadoCuadroP04.setTextColor(Color.parseColor(if (p04) "#B42318" else "#027A48"))
        tvEstadoCuadroP04.setBackgroundColor(Color.parseColor(if (p04) "#FEF3F2" else "#EDFDF5"))

        val p05 = estadosPuertasDemo["P2-05"] ?: false
        tvEstadoCuadroP05.text = if (p05) "05 Abierta" else "05 Cerrada"
        tvEstadoCuadroP05.setTextColor(Color.parseColor(if (p05) "#B42318" else "#027A48"))
        tvEstadoCuadroP05.setBackgroundColor(Color.parseColor(if (p05) "#FEF3F2" else "#EDFDF5"))

        val p06 = estadosPuertasDemo["P2-06"] ?: false
        tvEstadoCuadroP06.text = if (p06) "06 Abierta" else "06 Cerrada"
        tvEstadoCuadroP06.setTextColor(Color.parseColor(if (p06) "#B42318" else "#027A48"))
        tvEstadoCuadroP06.setBackgroundColor(Color.parseColor(if (p06) "#FEF3F2" else "#EDFDF5"))
    }

    private fun escucharEstadoPuertaEnPlano() {
        dbRef.child("Puerta").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val desbloqueada = snapshot.child("desbloqueada").getValue(Boolean::class.java) ?: false
                    estadosPuertasDemo["P2-04"] = desbloqueada
                    actualizarUISeleccionada()
                    actualizarCuadrosPlano()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        dbRef.child("PuertasDemo").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (doorSnap in snapshot.children) {
                        val key = doorSnap.key ?: continue
                        val des = doorSnap.child("desbloqueada").getValue(Boolean::class.java) ?: false
                        estadosPuertasDemo[key] = des
                    }
                    actualizarUISeleccionada()
                    actualizarCuadrosPlano()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun escucharSensoresEnPlano() {
        dbRef.child("Sensores").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    humoActual = snapshot.child("humo").getValue(Boolean::class.java) ?: false
                    temperaturaActual = snapshot.child("temperatura").getValue(Double::class.java) ?: 22.0
                    nivelFuegoActual = snapshot.child("nivel").getValue(String::class.java) ?: "Normal"

                    // Si la temperatura >= 50 °C o el nivel es "Alta", mostrar Banner de Alerta Crítica
                    if (temperaturaActual >= 50.0 || nivelFuegoActual.uppercase() == "ALTA") {
                        cardAlertaCriticaPlano.visibility = View.VISIBLE
                    } else {
                        cardAlertaCriticaPlano.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun iniciarLlamadaEmergencia() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            realizarLlamadaDirecta()
        } else {
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun realizarLlamadaDirecta() {
        try {
            AuditoriaHelper.registrarEvento(
                evento = "Llamada de emergencia realizada a $NUMERO_EMERGENCIA desde Plano",
                tipo = "EMERGENCIA"
            )
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$NUMERO_EMERGENCIA"))
            startActivity(intent)
        } catch (e: Exception) {
            abrirMarcadorLlamada()
        }
    }

    private fun abrirMarcadorLlamada() {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$NUMERO_EMERGENCIA"))
        startActivity(intent)
    }
}
