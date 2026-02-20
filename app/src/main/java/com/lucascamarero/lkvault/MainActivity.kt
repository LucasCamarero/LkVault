package com.lucascamarero.lkvault

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lucascamarero.lkvault.screens.CheckUsb
import com.lucascamarero.lkvault.screens.SplashScreen
import com.lucascamarero.lkvault.storage.UsbStorageManager
import com.lucascamarero.lkvault.ui.theme.LkVaultTheme
import com.lucascamarero.lkvault.viewmodels.LanguageViewModel
import kotlinx.coroutines.delay

/**
 * Actividad principal de la aplicación.
 *
 * Gestiona el flujo inicial de arranque en función del estado del almacenamiento USB:
 *
 * - Si el USB no está disponible → muestra [CheckUsb]
 * - Si el USB está disponible → muestra [SplashScreen]
 * - Tras el splash → inicia la navegación principal de la app
 *
 * También implementa detección continua de inserción y retirada del USB
 * para mantener la integridad del almacenamiento del vault.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Punto de entrada de la actividad.
     *
     * Inicializa el entorno Compose, el gestor de almacenamiento USB y el estado
     * global de la interfaz antes de mostrar el contenido de la aplicación.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LkVaultTheme(dynamicColor = false) {

                val context = LocalContext.current
                val usbStorageManager = remember { UsbStorageManager(context) }

                /**
                 * ViewModel global encargado de la gestión del idioma.
                 */
                val languageViewModel: LanguageViewModel = viewModel()

                /**
                 * Estado que indica si el almacenamiento USB es accesible.
                 */
                var usbReady by remember { mutableStateOf(usbStorageManager.isAccessible()) }

                /**
                 * Controla la visualización del splash tras validar el USB.
                 */
                var showSplash by rememberSaveable { mutableStateOf(true) }

                /**
                 * Observador continuo del estado real del USB.
                 *
                 * Permite detectar inserción y retirada del almacenamiento en caliente
                 * sin necesidad de reiniciar la aplicación.
                 */
                LaunchedEffect(usbStorageManager) {
                    while (true) {
                        delay(1500)

                        val state = usbStorageManager.isAccessible()

                        if (usbReady && !state) {
                            // USB retirado
                            usbReady = false
                            showSplash = true
                        } else if (!usbReady && state) {
                            // USB insertado nuevamente
                            usbReady = true
                            showSplash = true
                        }
                    }
                }

                /**
                 * Contenedor principal de la interfaz.
                 */
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {

                        /**
                         * Bloqueo de la aplicación mientras el USB no esté disponible.
                         */
                        !usbReady -> {
                            CheckUsb(
                                onUsbReady = { usbReady = true }
                            )
                        }

                        /**
                         * Pantalla splash mostrada tras validar el almacenamiento.
                         */
                        showSplash -> {
                            SplashScreen(
                                onTimeout = { showSplash = false }
                            )
                        }

                        /**
                         * Navegación principal de la aplicación.
                         */
                        else -> {
                            ScreenManager(languageViewModel = languageViewModel)
                        }
                    }
                }
            }
        }
    }
}