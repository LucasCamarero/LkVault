package com.lucascamarero.lkvault.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.security.VaultUnlockManager

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
// Esta pantalla permite al usuario introducir su contraseña maestra
// para desbloquear el vault. La contraseña se utiliza para derivar
// una clave mediante Argon2 y reconstruir el secreto necesario para
// acceder a los datos cifrados.
@Composable
fun LoginScreen(
    navController: NavController
) {

    // Obtiene el contexto actual de la aplicación dentro de Compose.
    val context = LocalContext.current

    // Se obtiene la Activity actual para poder cerrar la aplicación
    // en caso de que se exceda el número máximo de intentos.
    val activity = context as Activity

    // Estado que almacena la contraseña introducida por el usuario.
    val password = remember { mutableStateOf("") }

    // Estado que indica si debe mostrarse el mensaje de error.
    var error by remember { mutableStateOf(false) }

    // Número de intentos restantes permitidos antes de cerrar la app.
    var attemptsLeft by remember { mutableStateOf(3) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Título de la pantalla de desbloqueo del vault.
        Text(
            stringResource(id = R.string.login),
            color = MaterialTheme.colorScheme.primaryContainer,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Campo de texto para introducir la contraseña maestra.
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = {
                Text(
                    text = stringResource(id = R.string.maestra),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(26.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Botón que inicia el proceso de desbloqueo del vault.
        Button(

            onClick = {

                // Instancia del gestor encargado de reconstruir el secreto del vault.
                val unlockManager = VaultUnlockManager(context)

                // Se intenta desbloquear el vault utilizando la contraseña introducida.
                val masterKey = unlockManager.unlockVault(password.value)

                // Si el resultado no es null significa que la autenticación ha sido correcta.
                if (masterKey != null) {

                    // Se limpia el estado de error.
                    error = false

                    // Navegación hacia la pantalla principal de contraseñas.
                    navController.navigate("password") {

                        // Se elimina la pantalla de login del backstack
                        // para evitar volver atrás después de autenticarse.
                        popUpTo("login") { inclusive = true }
                    }

                } else {

                    // Si la autenticación falla se reduce el número de intentos restantes.
                    attemptsLeft--

                    // Si no quedan intentos se cierra completamente la aplicación.
                    if (attemptsLeft <= 0) {
                        activity.finish()
                        return@Button
                    }

                    // Se activa el mensaje de error en pantalla.
                    error = true
                }
            },
            enabled = password.value.isNotBlank(),
            modifier = Modifier.height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp
            )
        ) {
            Text(
                text = stringResource(id = R.string.boton_login),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (error) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = stringResource(id = R.string.error_login) + " $attemptsLeft",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}