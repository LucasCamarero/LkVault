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
@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    // Animación de escala (tamaño) del logo
    val scale = remember { Animatable(0.1f) }

    // Animación de opacidad (transparencia)
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Lanzamos la animación de fade-in en paralelo
        launch {
            alpha.animateTo(
                1f,
                animationSpec = tween(durationMillis = 1200)
            )
        }
        // Animación de pequeño a muy grande (zoom inicial)
        scale.animateTo(
            2.1f,
            animationSpec = tween(
                durationMillis = 2800,
                easing = LinearOutSlowInEasing
            )
        )
        // Pausa para que el logo grande se quede visible un momento
        delay(400)
        // Animación de grande a tamaño normal
        scale.animateTo(
            0.9f,
            animationSpec = tween(
                durationMillis = 1700,
                easing = LinearOutSlowInEasing
            )
        )
        // Tiempo extra antes de pasar a la siguiente pantalla
        delay(1300)
        // Avisamos en MainActivity que ya puede cambiar de pantalla
        onTimeout()
    }

    // Contenedor
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {

        // Contenido centrado en vertical
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Icono pequeño superior
            Image(
                painter = painterResource(R.drawable.android),
                contentDescription = "Android",
                modifier = Modifier.size(45.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Logo principal con animaciones de escala y opacidad
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .scale(scale.value)   // Se aplica la animación de tamaño
                    .alpha(alpha.value)   // Se aplica la animación de transparencia
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Nombre del autor
            Text(
                text = "Lucas Camarero",
                style = Typography2.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo
            Text(
                text = "Multiplatform Developer",
                style = Typography2.bodySmall,
                color = Color.White
            )
        }
    }
}