package com.lucascamarero.lkvault.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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

@Composable
fun MasterPasswordScreen(
    navController: NavController
) {

    val context = LocalContext.current
    val storageManager = UsbStorageManager(context)
    val vaultViewModel: VaultViewModel = viewModel()

    val prefs = context.getSharedPreferences(
        "usb_prefs",
        Context.MODE_PRIVATE
    )

    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }

    var recoveryKey by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }

    val passwordsMatch =
        password.value == confirmPassword.value && password.value.isNotBlank()

    val showMismatch =
        confirmPassword.value.isNotEmpty() && password.value != confirmPassword.value

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->

        if (uri != null) {

            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            prefs.edit()
                .putString("usb_uri", uri.toString())
                .apply()

            recoveryKey = initializeVault(
                context,
                uri,
                storageManager,
                password.value
            )

            vaultViewModel.checkVault()
        }
    }

    var emailSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (recoveryKey == null) {

            Text(
                stringResource(id = R.string.vault1) + " " + stringResource(id = R.string.vault2),
                color = MaterialTheme.colorScheme.primaryContainer,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

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

            if (showMismatch) {

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(id = R.string.error_maestra),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { launcher.launch(null) },
                enabled = passwordsMatch,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                     text = stringResource(id = R.string.boton_vault),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        } else {

            Text(
                text = stringResource(id = R.string.email_text),
                color = MaterialTheme.colorScheme.primaryContainer,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        "Email",
                        style = MaterialTheme.typography.labelLarge
                    )},
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {

                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                        putExtra(Intent.EXTRA_SUBJECT, "LkVault Recovery Key")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "$recoveryKey"
                        )
                    }

                    context.startActivity(
                        Intent.createChooser(intent, "Enviar Recovery Key")
                    )

                    emailSent = true;

                },
                enabled = email.isNotBlank() && !emailSent,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    stringResource(id = R.string.email_button),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    stringResource(id = R.string.email_button2),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

}

private fun initializeVault(
    context: Context,
    treeUri: Uri,
    storageManager: UsbStorageManager,
    password: String
): String {

    val keyDerivation = KeyDerivation()
    val cryptoManager = VaultCryptoManager(context)

    val salt = keyDerivation.generateSalt()

    val derivedKey = keyDerivation.deriveKey(
        password.toCharArray(),
        salt
    )

    val result = cryptoManager.initializeVault(derivedKey)

    val encryptedMasterKey = result.encryptedMasterKey
    val encryptedAux = result.encryptedAuxiliaryKey
    val shareUsb = result.usbShare
    val recoveryKey = result.recoveryKey

    val config = VaultConfig.create(salt)

    val configUri = storageManager.createFile(treeUri, "vault.config")
        ?: return recoveryKey

    storageManager.openOutput(configUri)?.use { output ->

        val tempFile = File.createTempFile("vault", ".config", context.cacheDir)

        config.save(tempFile)

        output.write(tempFile.readBytes())

        tempFile.delete()
    }

    val auxUri = storageManager.createFile(treeUri, "auxiliary.enc")
        ?: return recoveryKey

    storageManager.openOutput(auxUri)?.use {
        it.write(encryptedAux)
    }

    val masterUri = storageManager.createFile(treeUri, "masterkey.enc")
        ?: return recoveryKey

    storageManager.openOutput(masterUri)?.use {
        it.write(encryptedMasterKey)
    }

    val shareUri = storageManager.createFile(treeUri, "masterkey.share")
        ?: return recoveryKey

    storageManager.openOutput(shareUri)?.use {
        it.write(shareUsb)
    }

    return recoveryKey

}
