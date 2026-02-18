package com.lucascamarero.lkvault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.lucascamarero.lkvault.models.AppLanguage
import com.lucascamarero.lkvault.screens.AudioScreen
import com.lucascamarero.lkvault.screens.DocumentScreen
import com.lucascamarero.lkvault.screens.ImageScreen
import com.lucascamarero.lkvault.screens.PasswordScreen
import com.lucascamarero.lkvault.screens.VideoScreen
import com.lucascamarero.lkvault.ui.theme.Typography2
import com.lucascamarero.lkvault.viewmodels.LanguageViewModel
import com.lucascamarero.lkvault.R

@Composable
fun ScreenManager(languageViewModel: LanguageViewModel) {

    val navController = rememberNavController()

    Scaffold(
        topBar = { BarraSuperior(languageViewModel) },
        bottomBar = { BarraInferior(navController) }
    ) { innerPadding ->

        // Contenedor principal donde se muestra la pantalla activa
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Sistema de navegación entre pantallas
            NavHost(
                navController = navController,
                startDestination = "password"
            ) {
                composable("password") {
                    PasswordScreen(navController)
                }
                composable("image") {
                    ImageScreen(navController)
                }
                composable("video") {
                    VideoScreen(navController)
                }
                composable("audio") {
                    AudioScreen(navController)
                }
                composable("doc") {
                    DocumentScreen(navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(languageViewModel: LanguageViewModel) {

    val context = LocalContext.current
    val version = remember { getAppVersion(context) }

    var expanded by remember { mutableStateOf(false) }
    val currentLanguage = languageViewModel.currentLanguage

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
            // Selector de idioma
            Box {

                IconButton(onClick = { expanded = true }) {
                    Icon(
                        painter = painterResource(
                            id = if (currentLanguage == AppLanguage.CASTELLANO)
                                R.drawable.spain_flag
                            else
                                R.drawable.england_flag
                        ),
                        contentDescription = "Seleccionar idioma",
                        modifier = Modifier.size(26.dp),
                        tint = Color.Unspecified
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    DropdownMenuItem(
                        text = {
                            Icon(
                                painter = painterResource(R.drawable.spain_flag),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        },
                        onClick = {
                            languageViewModel.changeLanguage(AppLanguage.CASTELLANO)
                            expanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Icon(
                                painter = painterResource(R.drawable.england_flag),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        },
                        onClick = {
                            languageViewModel.changeLanguage(AppLanguage.INGLES)
                            expanded = false
                        }
                    )
                }
            }

            // Botón de ayuda
            IconButton(onClick = { }) {
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

// Barra Inferior
@Composable
fun BarraInferior(navController: NavHostController) {

    // variables para poder cambiar de color los items al ser seleccionados
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar (containerColor = MaterialTheme.colorScheme.primaryContainer){

        // Contraseñas
        NavigationBarItem(
            icon = { Icon(Icons.Default.Lock,
                contentDescription = "Password",
                modifier = Modifier.size(32.dp)) },
            selected = currentRoute == "password",
            onClick = {
                navController.navigate("password") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.background,
                unselectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
                indicatorColor = Color.Transparent
            )
        )

        // Imágenes
        NavigationBarItem(
            icon = { Icon(Icons.Default.Image,
                contentDescription = "Image",
                modifier = Modifier.size(32.dp)) },
            selected = currentRoute == "image",
            onClick = {
                navController.navigate("image") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.background,
                unselectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
                indicatorColor = Color.Transparent
            )
        )

        // Vídeos
        NavigationBarItem(
            icon = { Icon(Icons.Default.Videocam,
                contentDescription = "Video",
                modifier = Modifier.size(32.dp)) },
            selected = currentRoute == "video",
            onClick = {
                navController.navigate("video") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.background,
                unselectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
                indicatorColor = Color.Transparent
            )
        )

        // Audios
        NavigationBarItem(
            icon = { Icon(Icons.Default.Headphones,
                contentDescription = "Audio",
                modifier = Modifier.size(32.dp)) },
            selected = currentRoute == "audio",
            onClick = {
                navController.navigate("audio") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.background,
                unselectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
                indicatorColor = Color.Transparent
            )
        )

        // Documentos
        NavigationBarItem(
            icon = { Icon(Icons.Default.Description,
                contentDescription = "Doc",
                modifier = Modifier.size(32.dp)) },
            selected = currentRoute == "doc",
            onClick = {
                navController.navigate("doc") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.background,
                unselectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
                indicatorColor = Color.Transparent
            )
        )
    }
}