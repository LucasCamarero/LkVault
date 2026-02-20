package com.lucascamarero.lkvault.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.storage.UsbStorageManager
import kotlinx.coroutines.delay

/**
 * Pantalla encargada de validar la presencia del almacenamiento USB requerido por la aplicación.
 *
 * Permite:
 * - seleccionar el directorio del vault mediante SAF
 * - inicializar el almacenamiento la primera vez
 * - detectar automáticamente la inserción del USB
 *
 * Mientras el USB no esté accesible, la aplicación permanece bloqueada en esta pantalla.
 *
 * @param onUsbReady callback invocado cuando el almacenamiento USB se encuentra disponible.
 */
@Composable
fun CheckUsb(
    onUsbReady: () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { UsbStorageManager(context) }

    /**
     * Estado que indica si el almacenamiento USB es accesible actualmente.
     */
    var accessible by remember { mutableStateOf(manager.isAccessible()) }

    /**
     * Launcher SAF para permitir al usuario seleccionar la carpeta del vault.
     *
     * Tras la selección:
     * - se guarda la URI persistente
     * - se inicializa el almacenamiento (archivo sentinela)
     * - se valida la accesibilidad real
     */
    val usbPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let {
                manager.saveUri(it)

                /*
                 Inicializamos el vault mínimo (archivo sentinela)
                 */
                manager.initializeIfNeeded()

                /*
                 Validación real antes de continuar
                 */
                accessible = manager.isAccessible()

                if (accessible) {
                    onUsbReady()
                }
            }
        }

    /**
     * Revalidación periódica del almacenamiento USB.
     *
     * Permite detectar inserción del dispositivo sin intervención del usuario.
     * El polling se detiene en cuanto el almacenamiento vuelve a estar accesible.
     */
    LaunchedEffect(accessible) {
        if (!accessible) {
            while (true) {
                delay(1500)

                val state = manager.isAccessible()

                if (state) {
                    accessible = true
                    onUsbReady()
                    break
                }
            }
        }
    }

    /**
     * Interfaz visual que informa al usuario de la necesidad de insertar el USB
     * y proporciona acceso al selector SAF.
     */
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(
                text = stringResource(id = R.string.usb),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        item { Spacer(Modifier.height(24.dp)) }

        item {
            Button(
                onClick = { usbPicker.launch(null) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(id = R.string.boton_usb),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}