package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.repository.AuthRepository

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    var intentosFallidos by mutableStateOf(0)
        private set

    var isBlocked by mutableStateOf(false)
        private set

    var msgError by mutableStateOf<String?>(null)
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    fun iniciarSesion(correo: String, clave: String) {
        if (isBlocked) return

        // Verificar si usuario existe
        if (!authRepository.UserExists(correo)) {
            msgError = "Correo no existe. Contacta a tu supervisor o a RRHH."
            return
        }

        // Si existe, validar contrasenha
        if (authRepository.validarCredenciales(correo, clave)) {
            msgError = null
            loginSuccess = true
        } else {
            intentosFallidos++
            if (intentosFallidos >= 3) {
                isBlocked = true
                msgError = "Acceso bloqueado, contacta a tu supervisor."
            } else {
                msgError = "Contrasenha incorrecta. Intento $intentosFallidos de 3."
            }
        }
    }

    fun cerrarSesion() {
        loginSuccess = false
        msgError = null
        intentosFallidos = 0
    }
}