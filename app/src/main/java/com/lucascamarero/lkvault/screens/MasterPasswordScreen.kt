package com.lucascamarero.lkvault.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.utils.UsbStorageManager
import com.lucascamarero.lkvault.viewmodels.VaultViewModel
import com.lucascamarero.lkvault.security.*
import java.io.File

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
// Esta pantalla se muestra cuando el vault todavía no ha sido inicializado.
// Permite al usuario crear su contraseña maestra y seleccionar el dispositivo
// USB donde se almacenará la estructura del vault.
@Composable
fun MasterPasswordScreen(
    navController: NavController
) {

    // Contexto actual de la aplicación dentro del entorno Compose.
    val context = LocalContext.current

    // Gestor encargado de las operaciones de lectura y escritura en el USB.
    val storageManager = UsbStorageManager(context)

    // ViewModel que permite consultar si el vault ya está inicializado.
    val vaultViewModel: VaultViewModel = viewModel()

    // Acceso a SharedPreferences donde se almacenará la URI persistente del USB.
    val prefs = context.getSharedPreferences(
        "usb_prefs",
        Context.MODE_PRIVATE
    )

    // Estado que almacena la contraseña introducida por el usuario.
    val password = remember { mutableStateOf("") }

    // Estado que almacena la confirmación de la contraseña.
    val confirmPassword = remember { mutableStateOf("") }

    // Comprueba si ambas contraseñas coinciden y no están vacías.
    val passwordsMatch =
        password.value == confirmPassword.value && password.value.isNotBlank()

    // Indica si debe mostrarse el mensaje de error de contraseñas distintas.
    val showMismatch =
        confirmPassword.value.isNotEmpty() && password.value != confirmPassword.value

    // Launcher que abre el selector de directorios del sistema (SAF).
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->

        // Callback ejecutado cuando el usuario selecciona un directorio.
        if (uri != null) {

            // Se obtiene permiso persistente de lectura y escritura sobre el directorio.
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            // Se guarda la URI seleccionada en SharedPreferences para futuras ejecuciones.
            prefs.edit()
                .putString("usb_uri", uri.toString())
                .apply()

            // Inicialización criptográfica del vault.
            initializeVault(context, uri, storageManager, password.value)

            // Se actualiza el estado del vault dentro del ViewModel.
            vaultViewModel.checkVault()

            // Navegación a la pantalla principal tras completar la inicialización.
            navController.navigate("password") {
                popUpTo("masterPassword") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Título de la pantalla
        Text(
            stringResource(id = R.string.vault1) + " " + stringResource(id = R.string.vault2),
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

        Spacer(modifier = Modifier.height(22.dp))

        // Campo de confirmación de contraseña.
        OutlinedTextField(
            value = confirmPassword.value,
            onValueChange = { confirmPassword.value = it },
            label = {
                Text(
                    text = stringResource(id = R.string.confirmacion_maestra),
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

        // Mensaje de error si las contraseñas no coinciden.
        if (showMismatch) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(id = R.string.error_maestra),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Botón que inicia el proceso de selección del USB e inicialización del vault.
        Button(
            onClick = { launcher.launch(null) },
            // Solo se habilita si ambas contraseñas coinciden.
            enabled = passwordsMatch,
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
                text = stringResource(id = R.string.boton_vault),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Función encargada de inicializar la estructura criptográfica del vault.
private fun initializeVault(
    context: Context,
    treeUri: android.net.Uri,
    storageManager: UsbStorageManager,
    password: String
) {

    // Instancia del sistema de derivación de claves (Argon2).
    val keyDerivation = KeyDerivation()

    // Gestor criptográfico encargado de inicializar el vault.
    val cryptoManager = VaultCryptoManager(context)

    // -------- Generación de Salt --------

    // Se genera un salt aleatorio para Argon2.
    val salt = keyDerivation.generateSalt()

    // -------- Derivación de clave --------

    // Se deriva una clave criptográfica a partir de la contraseña introducida.
    val derivedKey = keyDerivation.deriveKey(
        password.toCharArray(),
        salt
    )

    // -------- Inicialización criptográfica --------

    // Se inicializa la estructura criptográfica del vault.
    val result = cryptoManager.initializeVault(derivedKey)

    // Clave auxiliar cifrada.
    val encryptedAux = result.second

    // Share que se almacenará en el USB.
    val shareUsb = result.third

    // Configuración criptográfica del vault.
    val config = VaultConfig.create(salt)

    // -------- Escritura de vault.config --------

    val configUri = storageManager.createFile(treeUri, "vault.config")
        ?: return

    storageManager.openOutput(configUri)?.use { output ->

        // Archivo temporal necesario para reutilizar el método save().
        val tempFile = File.createTempFile("vault", ".config", context.cacheDir)

        config.save(tempFile)

        output.write(tempFile.readBytes())

        tempFile.delete()
    }

    // -------- Escritura de masterkey.enc --------

    val encUri = storageManager.createFile(treeUri, "masterkey.enc")
        ?: return

    storageManager.openOutput(encUri)?.use {
        it.write(encryptedAux)
    }

    // -------- Escritura de masterkey.share --------

    val shareUri = storageManager.createFile(treeUri, "masterkey.share")
        ?: return

    storageManager.openOutput(shareUri)?.use {
        it.write(shareUsb)
    }
}