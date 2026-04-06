package com.lucascamarero.lkvault.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.security.SecurityManager
import com.lucascamarero.lkvault.security.VaultUnlockManager
import com.lucascamarero.lkvault.viewmodels.SessionViewModel

// HU-16: FLUJO DE AUTENTICACIÓN Y RECONSTRUCCIÓN DE MASTER KEY
// HU-17: LIMITACIÓN DE INTENTOS DE ACCESO
// Pantalla de login donde el usuario introduce la contraseña maestra.
// Incluye control de intentos fallidos y bloqueo temporal.
@Composable
fun LoginScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel
) {

    val context = LocalContext.current

    // Gestor de seguridad (intentos y bloqueo)
    val securityManager = remember { SecurityManager(context) }

    // Estado del campo de contraseña
    val password = remember { mutableStateOf("") }

    // Estado de error
    var error by remember { mutableStateOf(false) }

    // Estados de control de seguridad
    var attemptsLeft by remember { mutableStateOf(securityManager.getAttemptsLeft()) }
    var isBlocked by remember { mutableStateOf(securityManager.isBlocked()) }
    var blockTime by remember { mutableStateOf(securityManager.getRemainingBlockTime()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {

            // Texto título login
            Text(
                stringResource(id = R.string.login),
                color = MaterialTheme.colorScheme.primaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo contraseña
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = {
                    Text(
                        stringResource(id = R.string.maestra),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                textStyle = MaterialTheme.typography.labelLarge,
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botón login
            Button(
                onClick = {

                    // Si está bloqueado, se cancela el intento
                    if (securityManager.isBlocked()) {
                        isBlocked = true
                        blockTime = securityManager.getRemainingBlockTime()
                        return@Button
                    }

                    // Intento de desbloqueo del vault
                    val unlockManager = VaultUnlockManager(context)
                    val masterKey = unlockManager.unlockVault(password.value)

                    if (masterKey != null) {

                        // Login correcto → reset de seguridad
                        securityManager.registerSuccess()

                        error = false
                        attemptsLeft = securityManager.getAttemptsLeft()
                        isBlocked = false

                        // Guardo la master Key en sesión
                        sessionViewModel.setMasterKey(masterKey)

                        // Navegación a pantalla principal
                        navController.navigate("password") {
                            popUpTo("login") { inclusive = true }
                        }

                    } else {

                        // Login fallido → registrar intento
                        securityManager.registerFailure()

                        error = true
                        attemptsLeft = securityManager.getAttemptsLeft()
                        isBlocked = securityManager.isBlocked()
                        blockTime = securityManager.getRemainingBlockTime()
                    }
                },
                enabled = password.value.isNotBlank() && !isBlocked,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    stringResource(id = R.string.boton_login),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Mensaje de error con intentos restantes
            if (error && !isBlocked) {
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = stringResource(id = R.string.error_login) + " $attemptsLeft",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Mensaje de bloqueo con tiempo restante
            if (isBlocked) {
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = stringResource(id = R.string.bloqueo) + " ${blockTime / 1000}s",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}