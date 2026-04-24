package com.lucascamarero.lkvault.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.security.vault.VaultRecoveryManager

// HU-14: GENERACIÓN Y GESTIÓN DE RECOVERY KEY
// HU-16: FLUJO DE AUTENTICACIÓN Y RECONSTRUCCIÓN DE MASTER KEY
// Pantalla que permite restaurar el acceso al vault mediante una Recovery Key.
// Gestiona el flujo:
// - Introducción de la Recovery Key por parte del usuario
// - Selección del dispositivo USB mediante SAF
// - Restauración de la share del dispositivo en almacenamiento seguro
// - Configuración de la URI del USB para futuras operaciones
// - Redirección al flujo de autenticación (login)
@Composable
fun RecoveryScreen(navController: NavController) {

    val context = LocalContext.current

    // Gestor de recuperación del vault (reconstrucción parcial del estado)
    val recoveryManager = VaultRecoveryManager(context)

    // Estado de la Recovery Key introducida
    var recoveryKey by remember { mutableStateOf("") }

    // Estado de error en el proceso de recuperación
    var error by remember { mutableStateOf(false) }

    // Launcher SAF para seleccionar el USB
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->

        if (uri != null) {

            // Se solicitan permisos persistentes sobre el USB
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            // Se intenta restaurar el acceso utilizando la Recovery Key
            val success = recoveryManager.restoreAccess(
                recoveryKey,
                uri
            )

            if (success) {

                error = false

                // Navegación al login para continuar con la autenticación
                navController.navigate("login") {
                    popUpTo("masterPassword") { inclusive = true }
                }

            } else {
                // Recovery Key inválida o error en el proceso
                error = true
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {

            // Texto explicativo del proceso de recuperación
            Text(
                stringResource(id = R.string.intro_recovery),
                color = MaterialTheme.colorScheme.primaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo de entrada de la Recovery Key
            OutlinedTextField(
                value = recoveryKey,
                onValueChange = { recoveryKey = it },
                label = {
                    Text(
                        stringResource(id = R.string.rk),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                textStyle = MaterialTheme.typography.labelLarge,
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

            // Botón para iniciar el proceso de recuperación
            Button(
                onClick = {
                    launcher.launch(null)
                },
                enabled = recoveryKey.isNotBlank(),
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
                    stringResource(id = R.string.recuperar),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Mensaje de error si la Recovery Key no es válida
            if (error) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    stringResource(id = R.string.validez_rv),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}