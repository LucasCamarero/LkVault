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

// HU-1: INICIALIZACIÓN DEL PROYECTO
// Activity principal de la aplicación.
// Se encarga de:
// - Inicializar el entorno Compose
// - Aplicar el tema global
// - Gestionar la Splash Screen
// - Lanzar el gestor de navegación principal (ScreenManager)
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita el modo edge-to-edge para usar toda la pantalla
        enableEdgeToEdge()

        // Punto de entrada de la UI en Jetpack Compose
        setContent {

            // Tema global de la aplicación
            LkVaultTheme(dynamicColor = false) {

                // ViewModel responsable de gestionar el idioma de la app
                val languageViewModel: LanguageViewModel = viewModel()

                // Estado que controla si se muestra la Splash Screen
                var showSplash by rememberSaveable { mutableStateOf(true) }

                // Contenedor base de la UI
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {/*
                    when {

                        // Pantalla inicial de presentación (Splash)
                        showSplash -> {
                            SplashScreen(
                                onTimeout = { showSplash = false }
                            )
                        }

                        // Flujo principal de la aplicación (navegación)
                        else -> {
                            ScreenManager(
                                languageViewModel = languageViewModel
                            )
                        }
                    }*/
                    ScreenManager(
                        languageViewModel = languageViewModel
                    )

                }
            }
        }
    }
}