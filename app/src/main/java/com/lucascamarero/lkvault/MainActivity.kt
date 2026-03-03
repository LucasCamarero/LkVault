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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lucascamarero.lkvault.screens.SplashScreen
import com.lucascamarero.lkvault.ui.theme.LkVaultTheme
import com.lucascamarero.lkvault.viewmodels.LanguageViewModel

// Activity principal de la aplicación.
//
// Responsabilidades:
// - Configurar el entorno visual (Edge-to-Edge).
// - Inicializar Compose.
// - Aplicar el tema global.
// - Mostrar SplashScreen inicial.
// - Delegar la navegación al ScreenManager.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite que la app dibuje detrás de las barras del sistema
        enableEdgeToEdge()

        setContent {

            // Tema global de la aplicación
            LkVaultTheme(dynamicColor = false) {

                // ViewModel encargado de la gestión del idioma
                val languageViewModel: LanguageViewModel = viewModel()

                // Controla si se muestra la pantalla de inicio
                var showSplash by rememberSaveable { mutableStateOf(true) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    when {
                        // Mientras showSplash sea true, se muestra la pantalla inicial
                        showSplash -> {
                            SplashScreen(
                                onTimeout = { showSplash = false }
                            )
                        }

                        // Una vez finalizado el splash, se inicia la navegación real
                        else -> {
                            ScreenManager(
                                languageViewModel = languageViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}