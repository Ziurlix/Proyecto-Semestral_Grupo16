package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.HomeScreen
import com.example.ui.LoginScreen
import com.example.ui.theme.AppTheme
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.LoginViewModel

import com.example.data.local.LocalOperationsStore
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val store = LocalOperationsStore(this)
        
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(store.isDarkMode()) }
            
            AppTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(loginViewModel) {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                            composable("dashboard") {
                                HomeScreen(
                                    email = loginViewModel.email,
                                    role = loginViewModel.rolActivo,
                                    viewModel = dashboardViewModel,
                                    isDarkMode = isDarkMode,
                                    onToggleDarkMode = { 
                                        isDarkMode = it
                                        store.setDarkMode(it)
                                    },
                                    onCerrarSesion = {
                                        loginViewModel.resetLoginState()
                                        navController.navigate("login") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
