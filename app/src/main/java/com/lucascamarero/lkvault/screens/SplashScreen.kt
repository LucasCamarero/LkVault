package com.lucascamarero.lkvault.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.ui.theme.Typography2

// HU-3: SPLASH SCREEN
// Esta pantalla muestra una animación inicial al arrancar la aplicación.
// Incluye animaciones de escala y opacidad sobre el logo y, tras completarse,
// notifica al sistema para continuar con la navegación principal.
@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    // Animación de escala del logo (efecto zoom)
    val scale = remember { Animatable(0.1f) }

    // Animación de opacidad (fade-in)
    val alpha = remember { Animatable(0f) }

    // Lanzamiento de animaciones al entrar en composición
    LaunchedEffect(Unit) {

        // Animación de aparición (fade-in) en paralelo
        launch {
            alpha.animateTo(
                1f,
                animationSpec = tween(durationMillis = 1200)
            )
        }

        // Animación de zoom inicial
        scale.animateTo(
            2.1f,
            animationSpec = tween(
                durationMillis = 2800,
                easing = LinearOutSlowInEasing
            )
        )

        // Pausa breve con el logo ampliado
        delay(400)

        // Reducción a tamaño final
        scale.animateTo(
            0.9f,
            animationSpec = tween(
                durationMillis = 1700,
                easing = LinearOutSlowInEasing
            )
        )

        // Tiempo adicional antes de continuar
        delay(1300)

        // Se notifica que la splash ha finalizado
        onTimeout()
    }

    // Contenedor principal centrado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {

        // Contenido vertical centrado
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Icono superior (branding secundario)
            Image(
                painter = painterResource(R.drawable.android),
                contentDescription = "Android",
                modifier = Modifier.size(45.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Logo principal con animaciones aplicadas
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Texto con nombre del autor
            Text(
                text = "Lucas Camarero",
                style = Typography2.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo profesional
            Text(
                text = "Multiplatform Developer",
                style = Typography2.bodySmall,
                color = Color.White
            )
        }
    }
}