package com.example.repository

class AuthRepository {
    private val correoValido = "admin@test.com"
    private val claveValida = "123456"

    fun UserExists(correo : String): Boolean {
        return correo.trim().equals(correoValido, ignoreCase = true)
    }

    fun validarCredenciales(correo: String, clave:String): Boolean {
        return UserExists(correo) && clave == claveValida
    }
}