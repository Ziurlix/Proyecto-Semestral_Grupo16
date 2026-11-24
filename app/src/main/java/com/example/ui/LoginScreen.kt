package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current

    // Launcher oficial de Google Play Services para inicio de sesión nativo
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            if ((account != null) && !account.email.isNullOrBlank()) {
                viewModel.onGoogleAuthSuccess(
                    googleEmail = account.email!!,
                    displayName = account.displayName ?: ""
                )
            } else {
                viewModel.iniciarSesionConGoogle(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModel.iniciarSesionConGoogle(context)
        }
    }

    val onGoogleClick: () -> Unit = {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModel.iniciarSesionConGoogle(context)
        }
    }

    LaunchedEffect(viewModel.loginSuccess) {
        if (viewModel.loginSuccess) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "VENTAS Y DISTRIBUCIÓN",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (viewModel.isRegisteringMode) "Crear nueva cuenta" else "Bienvenido de vuelta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (viewModel.isRegisteringMode) "Ingresa tus datos para registrarte" else "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            // Selector de Modo: Iniciar Sesión / Registrarse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !viewModel.isRegisteringMode,
                    onClick = { if (viewModel.isRegisteringMode) viewModel.toggleRegisteringMode() },
                    label = { Text("Iniciar Sesión", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = viewModel.isRegisteringMode,
                    onClick = { if (!viewModel.isRegisteringMode) viewModel.toggleRegisteringMode() },
                    label = { Text("Registrarse", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(4.dp))

            // Campo: Correo electrónico
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                enabled = !viewModel.isBlocked,
                isError = viewModel.msgError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Campo: Contraseña
            var passwordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(if (viewModel.isRegisteringMode) "Contraseña" else "Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        Icon(icon, contentDescription = if (passwordVisible) "Ocultar contraseña" else "Ver contraseña")
                    }
                },
                singleLine = true,
                enabled = !viewModel.isBlocked,
                isError = viewModel.msgError != null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Campo: Doble contraseña (sólo en modo Registro)
            if (viewModel.isRegisteringMode) {
                var confirmPasswordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = viewModel.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            val icon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            Icon(icon, contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Ver contraseña")
                        }
                    },
                    singleLine = true,
                    enabled = !viewModel.isBlocked,
                    isError = viewModel.msgError != null,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Selector de Rol
                var roleExpanded by remember { mutableStateOf(false) }
                val roles = listOf("Vendedor", "Administrador", "Supervisor", "Repartidor")

                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol asignado") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    viewModel.onRoleChange(role)
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Mensaje de Error
            viewModel.msgError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Botón principal de Iniciar Sesión / Registro (SIEMPRE ACTIVO salvo bloqueo de app)
            Button(
                onClick = {
                    if (viewModel.isRegisteringMode) {
                        viewModel.registrarUsuario()
                    } else {
                        viewModel.iniciarSesion()
                    }
                },
                enabled = viewModel.isLoginEnabled(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (viewModel.isRegisteringMode) "Registrar usuario" else "Ingresar",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            // Separador Google
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "  o continúa con  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Botón de Iniciar Sesión / Crear con Google
            OutlinedButton(
                onClick = onGoogleClick,
                enabled = !viewModel.isBlocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (viewModel.isRegisteringMode) "Crear cuenta con Google" else "Iniciar sesión con Google",
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Cuentas de Google previamente almacenadas para acceso rápido
            if (viewModel.googleAccounts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Cuenta Google guardada:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        viewModel.googleAccounts.forEach { googleEmail ->
                            SuggestionChip(
                                onClick = { viewModel.onGoogleAuthSuccess(googleEmail) },
                                label = { Text("Continuar como $googleEmail") },
                                icon = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Enlace de alternancia entre Iniciar Sesión y Registro
            TextButton(
                onClick = viewModel::toggleRegisteringMode,
                enabled = !viewModel.isBlocked
            ) {
                Text(
                    if (viewModel.isRegisteringMode) "¿Ya tienes una cuenta? Inicia sesión aquí"
                    else "¿No tienes una cuenta? Regístrate con correo o Google"
                )
            }

            Text(
                "Sistema de gestión de ventas y logística",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }

    // Modal de Diálogo Google Identity / Fallback para selección o ingreso de cuenta de Google
    if (viewModel.showGoogleFallbackPrompt) {
        GoogleServicesDialog(
            googleAccounts = viewModel.googleAccounts,
            onDismiss = { viewModel.dismissGoogleFallbackPrompt() },
            onAccountSelected = { googleEmail, name ->
                viewModel.dismissGoogleFallbackPrompt()
                viewModel.onGoogleAuthSuccess(googleEmail, name)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleServicesDialog(
    googleAccounts: List<String>,
    onDismiss: () -> Unit,
    onAccountSelected: (String, String) -> Unit
) {
    var inputEmail by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Servicios de Google Identity")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Selecciona una cuenta de Google guardada o ingresa tu cuenta de Google:",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (googleAccounts.isNotEmpty()) {
                    Text("Cuentas de Google en este dispositivo:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    googleAccounts.forEach { email ->
                        OutlinedButton(
                            onClick = { onAccountSelected(email, "") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(email)
                        }
                    }
                    HorizontalDivider()
                }

                OutlinedTextField(
                    value = inputEmail,
                    onValueChange = { inputEmail = it; errorText = null },
                    label = { Text("Correo de Google (ej: usuario@gmail.com)") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Nombre del usuario (opcional)") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = inputEmail.trim()
                    if (trimmed.isBlank() || !trimmed.contains("@")) {
                        errorText = "Ingresa un correo de Google válido."
                    } else {
                        onAccountSelected(trimmed, inputName.trim())
                    }
                }
            ) {
                Text("Continuar con Google")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
