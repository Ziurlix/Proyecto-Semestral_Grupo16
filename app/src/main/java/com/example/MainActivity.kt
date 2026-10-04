package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.HomeScreen
import com.example.ui.theme.TestTheme
import com.example.ui.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {

                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "login"
                        ) {
                            // Pantalla Login
                            composable("login") {
                                LoginScreen(
                                    onLoginSuccess = {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true}
                                        }
                                    }
                                )
                            }

                            // Pantalla principal
                            composable("home") {
                                HomeScreen(
                                    onCerrarSesion = {
                                        navController.navigate("login") {
                                            popUpTo("home") { inclusive = true}
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