package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    val authRepository = AuthRepository(application)

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var isRegisteringMode by mutableStateOf(false)
        private set
    var selectedRole by mutableStateOf("Vendedor")
        private set

    var intentosFallidos by mutableIntStateOf(0)
        private set
    var isBlocked by mutableStateOf(false)
        private set
    var msgError by mutableStateOf<String?>(null)
        private set
    var loginSuccess by mutableStateOf(false)
        private set
    var rolActivo by mutableStateOf("")
        private set

    var googleAccounts by mutableStateOf<List<String>>(emptyList())
        private set
    var showGoogleFallbackPrompt by mutableStateOf(false)

    init {
        actualizarCuentasGoogle()
    }

    fun onEmailChange(newValue: String) { email = newValue; msgError = null }
    fun onPasswordChange(newValue: String) { password = newValue; msgError = null }
    fun onConfirmPasswordChange(newValue: String) { confirmPassword = newValue; msgError = null }
    fun onRoleChange(newValue: String) { selectedRole = newValue }

    fun toggleRegisteringMode() {
        isRegisteringMode = !isRegisteringMode
        password = ""
        confirmPassword = ""
        msgError = null
    }

    // El botón de inicio de sesión o registro siempre debe estar activo si no está bloqueado por intentos fallidos
    fun isLoginEnabled(): Boolean = !isBlocked

    fun actualizarCuentasGoogle() {
        googleAccounts = authRepository.obtenerCuentasGoogleAlmacenadas()
    }

    fun iniciarSesion() {
        if (isBlocked) return
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            msgError = "Por favor ingresa tu correo electrónico y contraseña."
            return
        }

        if (!authRepository.userExists(trimmedEmail)) {
            msgError = "El usuario no está registrado."
            return
        }

        if (authRepository.validarCredenciales(trimmedEmail, trimmedPassword)) {
            msgError = null
            rolActivo = authRepository.obtenerRol(trimmedEmail)
            loginSuccess = true
        } else {
            intentosFallidos++
            msgError = if (intentosFallidos >= 3) {
                isBlocked = true
                "Acceso bloqueado por tres intentos fallidos. Reinicia la app para continuar."
            } else "Contraseña incorrecta. Intentos restantes: ${3 - intentosFallidos}."
        }
    }

    fun registrarUsuario() {
        if (isBlocked) return
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedConfirm = confirmPassword.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank() || trimmedConfirm.isBlank()) {
            msgError = "Por favor completa el correo y ambas contraseñas."
            return
        }

        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            msgError = "Por favor ingresa un correo electrónico válido."
            return
        }

        if (trimmedPassword != trimmedConfirm) {
            msgError = "Las contraseñas no coinciden. Verifica ambas contraseñas."
            return
        }

        if (trimmedPassword.length < 4) {
            msgError = "La contraseña debe tener al menos 4 caracteres."
            return
        }

        if (authRepository.userExists(trimmedEmail)) {
            msgError = "El correo ya está registrado. Utiliza la opción de Iniciar Sesión."
            return
        }

        val success = authRepository.registrarUsuario(trimmedEmail, trimmedPassword, selectedRole)
        if (success) {
            msgError = null
            rolActivo = selectedRole
            loginSuccess = true
        } else {
            msgError = "No se pudo registrar el usuario."
        }
    }

    fun iniciarSesionConGoogle(context: Context, webClientId: String = "") {
        if (isBlocked) return
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId.ifBlank { "1234567890-example.apps.googleusercontent.com" })
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                if ((credential is CustomCredential) && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val gEmail = googleIdTokenCredential.id
                    val name = googleIdTokenCredential.displayName ?: ""
                    onGoogleAuthSuccess(gEmail, name)
                } else {
                    showGoogleFallbackPrompt = true
                }
            } catch (e: GetCredentialException) {
                e.printStackTrace()
                showGoogleFallbackPrompt = true
            } catch (e: Exception) {
                e.printStackTrace()
                showGoogleFallbackPrompt = true
            }
        }
    }

    fun onGoogleAuthSuccess(googleEmail: String, displayName: String = "") {
        val trimmed = googleEmail.trim()
        if (trimmed.isBlank() || !trimmed.contains("@")) {
            msgError = "Correo de Google inválido."
            return
        }
        email = trimmed
        rolActivo = authRepository.registrarOSignInGoogle(trimmed, displayName, selectedRole)
        loginSuccess = true
        msgError = null
        actualizarCuentasGoogle()
    }

    fun dismissGoogleFallbackPrompt() {
        showGoogleFallbackPrompt = false
    }

    fun resetLoginState() {
        email = ""
        password = ""
        confirmPassword = ""
        isRegisteringMode = false
        loginSuccess = false
        rolActivo = ""
        intentosFallidos = 0
        isBlocked = false
        msgError = null
        actualizarCuentasGoogle()
    }
}
