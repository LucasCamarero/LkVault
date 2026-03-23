package com.lucascamarero.lkvault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.lucascamarero.lkvault.models.AppLanguage
import com.lucascamarero.lkvault.screens.ImageScreen
import com.lucascamarero.lkvault.screens.MasterPasswordScreen
import com.lucascamarero.lkvault.screens.PasswordScreen
import com.lucascamarero.lkvault.ui.theme.Typography2
import com.lucascamarero.lkvault.viewmodels.LanguageViewModel
import com.lucascamarero.lkvault.viewmodels.UsbViewModel
import android.content.Context
import com.lucascamarero.lkvault.utils.UsbStorageManager
import com.lucascamarero.lkvault.viewmodels.VaultViewModel
import androidx.compose.runtime.LaunchedEffect
import com.lucascamarero.lkvault.screens.LoginScreen
import com.lucascamarero.lkvault.screens.RecoveryScreen

// HU-4: SCREEN MANAGER: gestiona
// - el estado del USB
// - la navegación entre pantallas
// - las barras superior e inferior
@Composable
fun ScreenManager(languageViewModel: LanguageViewModel) {

    // Controlador de navegación para Compose Navigation
    val navController = rememberNavController()

    // ViewModel que expone el estado del USB
    val usbViewModel: UsbViewModel = viewModel()
    // Estado observable que indica si existe un USB válido conectado
    val usbConnected = usbViewModel.isUsbConnected.value

    // Obtiene el ViewModel encargado de gestionar el estado del vault.
    val vaultViewModel: VaultViewModel = viewModel()

    // LaunchedEffect se ejecuta cada vez que cambia el valor de usbConnected.
    LaunchedEffect(usbConnected) {

        // Si el USB está conectado se vuelve a comprobar si el vault ya está inicializado.
        if (usbConnected) {
            vaultViewModel.checkVault()
        }
    }

    // Estado observable que indica si el vault ya está inicializado.
    val vaultInitialized = vaultViewModel.vaultInitialized.value

    // LaunchedEffect se ejecuta cada vez que cambia el valor de vaultInitialized.
    LaunchedEffect(vaultInitialized) {

        if (vaultInitialized) {
            navController.navigate("login") {
                popUpTo("masterPassword") { inclusive = true }
            }
        }
    }

    // Obtiene el contexto actual de la aplicación dentro de Compose.
    val context = LocalContext.current

    // Instancia del gestor encargado de interactuar con el almacenamiento USB
    val storageManager = UsbStorageManager(context)

    // Acceso a las SharedPreferences donde se guarda la URI del USB
    val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)

    // Recupera la URI almacenada del USB.
    // Si es null significa que todavía no se ha concedido permiso al almacenamiento.
    val uriString = prefs.getString("usb_uri", null)

    Scaffold(
        // Barra superior siempre visible
        topBar = { BarraSuperior(languageViewModel) },

        // Barra inferior solo visible si el USB es válido
        bottomBar = {
            if (usbConnected) {
                BarraInferior(navController)
            }
        }
    ) { innerPadding ->

        // Contenedor principal
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Si no hay USB válido, se bloquea la aplicación
            if (!usbConnected) {

                // Pantalla informativa cuando no hay USB
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Text(
                            stringResource(id = R.string.usb1),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            stringResource(id = R.string.usb2),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

            // Navegación cuando el USB está conectado
            } else {
                NavHost(
                    navController = navController,
                    startDestination =
                        if (vaultInitialized) "login"
                        else "masterPassword"
                ) {

                    composable("masterPassword") {
                        MasterPasswordScreen(navController)
                    }

                    composable("login") {
                        LoginScreen(navController)
                    }

                    composable("recovery") {
                        RecoveryScreen(navController)
                    }

                    composable("password") {
                        PasswordScreen(navController)
                    }

                    composable("image") {
                        ImageScreen(navController)
                    }
                }
            }
        }
    }
}

// Barra superior
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(languageViewModel: LanguageViewModel) {

    val context = LocalContext.current
    val version = remember { getAppVersion(context) }

    // Controla la apertura del menú desplegable de idioma
    var expanded by remember { mutableStateOf(false) }

    // Controla el idioma seleccionado en la app
    val currentLanguage = languageViewModel.currentLanguage

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        title = {
            Row {
                // Nombre de la app
                Text("Lk Vault ", style = Typography2.titleSmall)

                // Versión actual
                Text(
                    text = "v$version",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                )
            }
        },
        actions = {

            Box {

                // Botón que muestra la bandera actual
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

                // Menú desplegable para cambiar idioma
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(50.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {

                    // Opción Español
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

                    // Opción Inglés
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
        }
    )
}

// Barra inferior
@Composable
fun BarraInferior(navController: NavHostController) {

    // Obtiene la ruta actual para marcar el item seleccionado
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {

        // Item de contraseñas
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Password",
                    modifier = Modifier.size(32.dp)
                )
            },
            selected = currentRoute == "password",
            onClick = {
                navController.navigate("password") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.background,
                indicatorColor = Color.Transparent
            )
        )

        // Item de imágenes
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Image",
                    modifier = Modifier.size(32.dp)
                )
            },
            selected = currentRoute == "image",
            onClick = {
                navController.navigate("image") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.background,
                indicatorColor = Color.Transparent
            )
        )
    }
}