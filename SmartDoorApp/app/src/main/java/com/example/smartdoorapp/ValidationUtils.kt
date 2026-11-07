package com.example.smartdoorapp

object ValidationUtils {

    fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    fun isRutValid(rut: String): Boolean {
        if (rut.isBlank()) return false
        val cleanRut = rut.replace(".", "").replace("-", "").uppercase()
        return cleanRut.length >= 8
    }
}
