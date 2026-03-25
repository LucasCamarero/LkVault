package com.lucascamarero.lkvault.screens

import android.content.Context
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.security.*
import com.lucascamarero.lkvault.utils.UsbStorageManager
import com.lucascamarero.lkvault.viewmodels.VaultViewModel
import java.io.File

// HU-15: FLUJO COMPLETO DE INICIALIZACIÓN CRIPTOGRÁFICA
// HU-14: GENERACIÓN Y GESTIÓN DE RECOVERY KEY
// Pantalla encargada de:
// - Crear contraseña maestra
// - Inicializar el vault en el USB
// - Generar y enviar la Recovery Key
@Composable
fun MasterPasswordScreen(
    navController: NavController
) {

    val context = LocalContext.current
    val storageManager = UsbStorageManager(context)
    val vaultViewModel: VaultViewModel = viewModel()

    // Preferencias donde se guarda la URI del USB
    val prefs = context.getSharedPreferences(
        "usb_prefs",
        Context.MODE_PRIVATE
    )

    // Estados de contraseña
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }

    // Estados de recovery y email
    var recoveryKey by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }

    // Validación de contraseñas
    val passwordsMatch =
        password.value == confirmPassword.value && password.value.isNotBlank()

    val showMismatch =
        confirmPassword.value.isNotEmpty() &&
                password.value != confirmPassword.value

    // Launcher SAF para seleccionar el USB
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->

        if (uri != null) {

            // Se guardan permisos persistentes
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            // Se guarda la URI del USB
            prefs.edit()
                .putString("usb_uri", uri.toString())
                .apply()

            // Se inicializa el vault
            recoveryKey = initializeVault(
                context,
                uri,
                storageManager,
                password.value
            )

            // Se actualiza el estado del vault
            vaultViewModel.checkVault()
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

            // -------- CREACIÓN DE CONTRASEÑA --------
            if (recoveryKey == null) {

                // Texto informativo inicial
                Text(
                    stringResource(id = R.string.vault_text),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Campo contraseña maestra
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

                Spacer(modifier = Modifier.height(28.dp))

                // Campo confirmación contraseña
                OutlinedTextField(
                    value = confirmPassword.value,
                    onValueChange = { confirmPassword.value = it },
                    label = {
                        Text(
                            stringResource(id = R.string.confirmacion_maestra),
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

                // Mensaje de error si no coinciden
                if (showMismatch) {
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = stringResource(id = R.string.error_maestra),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Botón crear vault
                Button(
                    onClick = { launcher.launch(null) },
                    enabled = passwordsMatch,
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
                        stringResource(id = R.string.boton_vault),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(150.dp))

                // Texto recuperación
                Text(
                    stringResource(id = R.string.pregunta_olvido),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Navegación a recovery
                TextButton(
                    onClick = {
                        navController.navigate("recovery")
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.recovery_key),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

            } else {

                // -------- ENVÍO DE RECOVERY KEY --------

                // Texto informativo email
                Text(
                    text = stringResource(id = R.string.email_text),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Campo email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    enabled = !emailSent,
                    label = {
                        Text(
                            "Email",
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

                // Botón enviar email
                Button(
                    onClick = {

                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                            putExtra(Intent.EXTRA_SUBJECT, "LkVault Recovery Key")
                            putExtra(Intent.EXTRA_TEXT, recoveryKey)
                        }

                        context.startActivity(
                            Intent.createChooser(intent, "Enviar Recovery Key")
                        )

                        emailSent = true
                    },
                    enabled = email.isNotBlank() && !emailSent,
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        stringResource(id = R.string.email_button),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Botón continuar
                Button(
                    onClick = {
                        navController.navigate("password") {
                            popUpTo("masterPassword") { inclusive = true }
                        }
                    },
                    enabled = emailSent,
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        stringResource(id = R.string.email_button2),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Inicialización completa del vault (HU-15)
private fun initializeVault(
    context: Context,
    treeUri: Uri,
    storageManager: UsbStorageManager,
    password: String
): String {

    // Derivación de clave desde contraseña
    val keyDerivation = KeyDerivation()
    val cryptoManager = VaultCryptoManager(context)

    val salt = keyDerivation.generateSalt()

    val passwordChars = password.toCharArray()

    val derivedKey = keyDerivation.deriveKey(
        passwordChars,
        salt
    )

    // Limpieza password
    passwordChars.fill('\u0000')

    // Inicialización criptográfica completa
    val result = cryptoManager.initializeVault(
        derivedKey,
        salt
    )

    val config = VaultConfig.create(salt)

    // Escritura de archivos en USB
    val configUri = storageManager.createFile(treeUri, "vault.config")
        ?: return result.recoveryKey

    storageManager.openOutput(configUri)?.use { output ->

        val tempFile = File.createTempFile("vault", ".config", context.cacheDir)
        config.save(tempFile)

        output.write(tempFile.readBytes())
        tempFile.delete()
    }

    val auxUri = storageManager.createFile(treeUri, "auxiliary.enc")
        ?: return result.recoveryKey

    storageManager.openOutput(auxUri)?.use {
        it.write(result.encryptedAuxiliaryKey)
    }

    val masterUri = storageManager.createFile(treeUri, "masterkey.enc")
        ?: return result.recoveryKey

    storageManager.openOutput(masterUri)?.use {
        it.write(result.encryptedMasterKey)
    }

    val shareUri = storageManager.createFile(treeUri, "masterkey.share")
        ?: return result.recoveryKey

    storageManager.openOutput(shareUri)?.use {
        it.write(result.usbShare)
    }

    // Limpieza derivedKey
    derivedKey.fill(0)

    return result.recoveryKey
}