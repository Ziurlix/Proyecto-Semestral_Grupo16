package com.example.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Repositorio de Autenticación con soporte para cuentas locales, dinámicas y Google Sign-In. */
class AuthRepository(context: Context) {
    private data class DemoAccount(val email: String, val password: String, val defaultRole: String)

    private val preferences = context.getSharedPreferences("ventas_distribucion_store", Context.MODE_PRIVATE)
    
    // Cuentas de pruebas definidas solo en código
    private val codeAccounts = listOf(
        DemoAccount("admin@guardian.test", "111111", "Administrador"),
        DemoAccount("admin@gmail.test", "111111", "Administrador"),
        DemoAccount("supervisor@gmail.test", "111111", "Supervisor"),
        DemoAccount("vendedor@gmail.test", "111111", "Vendedor"),
        DemoAccount("operador@gmail.test", "111111", "Repartidor")
    )

    private fun getRegisteredUsersJson(): JSONArray {
        val jsonStr = preferences.getString("dynamic_users", "[]") ?: "[]"
        return try {
            JSONArray(jsonStr)
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun saveRegisteredUsersJson(jsonArray: JSONArray) {
        preferences.edit().putString("dynamic_users", jsonArray.toString()).apply()
    }

    fun userExists(email: String): Boolean {
        val trimmed = email.trim()
        if (codeAccounts.any { it.email.equals(trimmed, ignoreCase = true) }) return true
        val users = getRegisteredUsersJson()
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(trimmed, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun validarCredenciales(email: String, password: String): Boolean {
        val trimmed = email.trim()
        val codeAcc = codeAccounts.firstOrNull { it.email.equals(trimmed, ignoreCase = true) }
        if (codeAcc != null) {
            return codeAcc.password == password
        }
        val users = getRegisteredUsersJson()
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(trimmed, ignoreCase = true)) {
                val isGoogle = user.optBoolean("is_google", false)
                if (isGoogle && password.isEmpty()) return true
                return user.optString("password") == password
            }
        }
        return false
    }

    fun registrarUsuario(email: String, password: String, role: String): Boolean {
        val trimmed = email.trim()
        if (userExists(trimmed)) return false
        
        val users = getRegisteredUsersJson()
        val newUser = JSONObject().apply {
            put("email", trimmed)
            put("password", password)
            put("role", role)
            put("is_google", false)
        }
        users.put(newUser)
        saveRegisteredUsersJson(users)
        preferences.edit().putString("user_role_$trimmed", role).apply()
        return true
    }

    fun registrarOSignInGoogle(googleEmail: String, displayName: String = "", role: String = "Vendedor"): String {
        val trimmed = googleEmail.trim()
        val users = getRegisteredUsersJson()
        var foundUser: JSONObject? = null
        var foundIndex = -1

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(trimmed, ignoreCase = true)) {
                foundUser = user
                foundIndex = i
                break
            }
        }

        if (foundUser != null) {
            foundUser.put("is_google", true)
            if (displayName.isNotBlank()) foundUser.put("display_name", displayName)
            users.put(foundIndex, foundUser)
            saveRegisteredUsersJson(users)
            return preferences.getString("user_role_$trimmed", foundUser.optString("role", role)) ?: role
        } else {
            val newUser = JSONObject().apply {
                put("email", trimmed)
                put("password", "")
                put("role", role)
                put("display_name", displayName)
                put("is_google", true)
            }
            users.put(newUser)
            saveRegisteredUsersJson(users)
            preferences.edit().putString("user_role_$trimmed", role).apply()
            return role
        }
    }

    fun obtenerRol(email: String): String {
        val trimmed = email.trim()
        val codeAcc = codeAccounts.firstOrNull { it.email.equals(trimmed, ignoreCase = true) }
        val defaultRole = codeAcc?.defaultRole ?: "Vendedor"

        val users = getRegisteredUsersJson()
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(trimmed, ignoreCase = true)) {
                return preferences.getString("user_role_$trimmed", user.optString("role", defaultRole)) ?: defaultRole
            }
        }
        return preferences.getString("user_role_$trimmed", defaultRole) ?: defaultRole
    }

    fun obtenerCuentasGoogleAlmacenadas(): List<String> {
        val googleEmails = mutableListOf<String>()
        val users = getRegisteredUsersJson()
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optBoolean("is_google", false)) {
                val emailStr = user.optString("email")
                if (emailStr.isNotBlank()) {
                    googleEmails.add(emailStr)
                }
            }
        }
        return googleEmails
    }
}
