package com.example.smartdoorapp

data class UserModel(
    val uid: String = "",
    val nombre: String = "",
    val rut: String = "",
    val email: String = "",
    val rol: String = "user", // "admin" o "user"
    val passwordHash: String = "",
    val salt: String = ""
) {
    fun esAdmin(): Boolean = rol.lowercase() == "admin"
}
