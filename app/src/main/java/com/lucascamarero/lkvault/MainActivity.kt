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

// HU-1: INICIALIZAR PROYECTO
// Activity principal de la aplicación
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LkVaultTheme(dynamicColor = false) {

                // ViewModel encargado de la gestión del idioma
                val languageViewModel: LanguageViewModel = viewModel()

                // Controla si se muestra la pantalla de inicio
                var showSplash by rememberSaveable { mutableStateOf(true) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
/*
                    when {
                        // Pantalla de presentación
                        showSplash -> {
                            SplashScreen(
                                onTimeout = { showSplash = false }
                            )
                        }
                        // Manager de navegación
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