package com.lucascamarero.lkvault

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.lucascamarero.lkvault.ui.theme.Typography2


@Composable
fun ScreenManager() {

    val navController = rememberNavController()

    Scaffold(
        topBar = { BarraSuperior() },
        bottomBar = { BarraInferior() }
    ) { innerPadding ->

        // Contenedor principal donde se muestra la pantalla activa
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
/*
            // Sistema de navegación entre pantallas
            NavHost(
                navController = navController,
                startDestination = "list"
            ) {
                composable("list") {
                    // Pantalla con la lista de emisoras
                    RadiosList(navController)
                }
            }*/
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior() {

    val context = LocalContext.current
    val version = remember { getAppVersion(context) }

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        title = {
            Row {
                Text("Lk Vault ", style = Typography2.titleSmall)
                Text(
                    text = "v$version",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                )
            }
        },
        actions = {
            // Botón de ayuda
            IconButton(onClick = {

            }) {
            Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Ayuda",
                    tint = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    )
}

@Composable
fun BarraInferior() {

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {

    }
}