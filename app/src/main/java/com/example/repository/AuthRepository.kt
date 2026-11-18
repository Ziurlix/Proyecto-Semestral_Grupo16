package com.example.repository

import android.content.Context

/** Usuarios sintéticos para la evaluación. No utilizar estas credenciales en producción. */
class AuthRepository(context: Context) {
    private data class DemoAccount(val email: String, val password: String, val defaultRole: String)

    private val preferences = context.getSharedPreferences("ventas_distribucion_store", Context.MODE_PRIVATE)
    private val accounts = listOf(
        DemoAccount("admin@gmail.test", "111111", "Administrador"),
        DemoAccount("supervisor@gmail.test", "111111", "Supervisor"),
        DemoAccount("vendedor@gmail.test", "111111", "Vendedor"),
        DemoAccount("operador@gmail.test", "111111", "Repartidor")
    )

    fun userExists(email: String): Boolean = accounts.any { it.email.equals(email.trim(), ignoreCase = true) }

    fun validarCredenciales(email: String, password: String): Boolean =
        accounts.any { it.email.equals(email.trim(), ignoreCase = true) && it.password == password }

    fun obtenerRol(email: String): String {
        val account = accounts.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return "Repartidor"
        return preferences.getString("user_role_${account.email}", account.defaultRole) ?: account.defaultRole
    }
}
