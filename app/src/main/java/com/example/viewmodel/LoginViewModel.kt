package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.repository.AuthRepository

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var intentosFallidos by mutableStateOf(0)
        private set
    var isBlocked by mutableStateOf(false)
        private set
    var msgError by mutableStateOf<String?>(null)
        private set
    var loginSuccess by mutableStateOf(false)
        private set
    var rolActivo by mutableStateOf("")
        private set

    fun onEmailChange(newValue: String) { email = newValue; msgError = null }
    fun onPasswordChange(newValue: String) { password = newValue; msgError = null }
    fun isLoginEnabled(): Boolean = !isBlocked && email.isNotBlank() && password.isNotBlank()

    fun iniciarSesion() {
        if (isBlocked) return
        if (!authRepository.userExists(email)) {
            msgError = "El correo no está registrado en los usuarios de demostración."
            return
        }
        if (authRepository.validarCredenciales(email, password)) {
            msgError = null
            rolActivo = authRepository.obtenerRol(email)
            loginSuccess = true
        } else {
            intentosFallidos++
            msgError = if (intentosFallidos >= 3) {
                isBlocked = true
                "Acceso bloqueado por tres intentos fallidos. Cierra y vuelve a abrir la aplicación para reiniciar la demostración."
            } else "Contraseña incorrecta. Intentos restantes: ${3 - intentosFallidos}."
        }
    }

    fun resetLoginState() {
        email = ""
        password = ""
        loginSuccess = false
        rolActivo = ""
        intentosFallidos = 0
        isBlocked = false
        msgError = null
    }
}
