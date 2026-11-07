package com.example.smartdoorapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Esto conecta este archivo Kotlin con el diseño XML que acabas de crear
        setContentView(R.layout.activity_login)

        // 2. Buscamos los elementos visuales usando los ID que les dimos en el XML
        val etUsuarioRut = findViewById<EditText>(R.id.etUsuarioRut)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        // 3. Programamos la acción del botón "Ingresar"
        btnIngresar.setOnClickListener {

            // Capturamos el texto que el usuario escribió en las cajas
            val usuario = etUsuarioRut.text.toString()
            val contrasena = etContrasena.text.toString()

            // 4. Verificamos que los campos no estén vacíos
            if (usuario.isEmpty() || contrasena.isEmpty()) {
                // Muestra un mensaje emergente de error
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Muestra un mensaje de éxito temporal
                Toast.makeText(this, "Intentando ingresar con RUT: $usuario", Toast.LENGTH_SHORT).show()

                // Nota: Aquí programaremos después la validación real con base de datos
            }
        }

        // (Opcional) Programamos el texto de "Crear cuenta" para que haga algo al tocarlo
        tvCrearCuenta.setOnClickListener {
            Toast.makeText(this, "Ir a pantalla de registro...", Toast.LENGTH_SHORT).show()
        }
    }
}