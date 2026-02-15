package com.lucascamarero.lkvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lucascamarero.lkvault.screens.SplashScreen
import com.lucascamarero.lkvault.ui.theme.LkVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LkVaultTheme(dynamicColor = false) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by rememberSaveable { mutableStateOf(true) }

                    when {
                        showSplash -> {
                            SplashScreen(
                                onTimeout = { showSplash = false }
                            )
                        }
                        else -> {
                            ScreenManager()
                        }
                    }
                }
            }
        }
    }
}