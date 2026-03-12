package com.lucascamarero.lkvault.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.utils.UsbStorageManager
import com.lucascamarero.lkvault.viewmodels.VaultViewModel

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// Pantalla encargada de:
// - inicializar el vault cuando todavía no existe
@Composable
fun MasterPasswordScreen(
    navController: NavController
) {

    // Obtiene el contexto actual de la aplicación dentro de Compose
    val context = LocalContext.current

    // Instancia del gestor de almacenamiento USB que utilizará SAF para crear archivos
    val storageManager = UsbStorageManager(context)

    // Obtiene el ViewModel encargado de gestionar el estado del vault
    val vaultViewModel: VaultViewModel = viewModel()

    // Accede a las SharedPreferences donde se guardará la URI del USB seleccionada
    val prefs = context.getSharedPreferences(
        "usb_prefs",
        Context.MODE_PRIVATE
    )

    // Launcher que abre el selector de almacenamiento del sistema (Storage Access Framework)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->

        // Callback que se ejecuta cuando el usuario selecciona un directorio
        if (uri != null) {

            // Se guarda permiso persistente para poder acceder al USB en futuras ejecuciones
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            // Se guarda la URI del USB en SharedPreferences para poder recuperarla después
            prefs.edit()
                .putString("usb_uri", uri.toString())
                .apply()

            // Se crean los archivos iniciales del vault dentro de la carpeta LkVault
            createVaultFiles(context, uri, storageManager)

            // Se actualiza el estado del vault en el ViewModel
            vaultViewModel.checkVault()

            // Se navega a la pantalla principal de contraseñas
            navController.navigate("password") {
                // Se elimina la pantalla actual del back stack para evitar volver atrás
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

        Text(
            stringResource(id = R.string.vault),
            color = MaterialTheme.colorScheme.primaryContainer,
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                // Lanza el selector de almacenamiento
                launcher.launch(null)
            },
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
    }
}

// Función encargada de crear los archivos iniciales del vault en el USB
private fun createVaultFiles(
    context: Context,
    treeUri: android.net.Uri,
    storageManager: UsbStorageManager
) {

    // Lista de archivos necesarios para inicializar el vault
    val files = listOf(
        "vault.config",
        "masterkey.enc",
        "masterkey.share"
    )

    // Recorre cada nombre de archivo de la lista
    for (name in files) {

        // Intenta crear el archivo dentro de la carpeta LkVault
        val uri = storageManager.createFile(treeUri, name) ?: continue

        // Abre un flujo de escritura hacia el archivo creado
        storageManager.openOutput(uri)?.use {

            // Escribe un array vacío para crear físicamente el archivo
            it.write(ByteArray(0))
        }
    }
}