package com.lucascamarero.lkvault

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.lucascamarero.lkvault.models.AppLanguage
import com.lucascamarero.lkvault.screens.*
import com.lucascamarero.lkvault.ui.theme.Typography2
import com.lucascamarero.lkvault.utils.UsbStorageManager
import com.lucascamarero.lkvault.viewmodels.*

// HU-4: IMPLEMENTACIÓN DE NAVEGACIÓN PRINCIPAL (TopBar + BottomBar)
// HU-6: DETECCIÓN Y VALIDACIÓN DE USB
// HU-7: CONTROL DEL ESTADO DEL VAULT
// Este componente central gestiona:
// - Navegación entre pantallas
// - Estado del USB
// - Estado de inicialización del vault
// - Visibilidad de barras de navegación
@Composable
fun ScreenManager(languageViewModel: LanguageViewModel) {

    val navController = rememberNavController()

    // ViewModel que expone el estado del USB
    val usbViewModel: UsbViewModel = viewModel()
    val usbConnected = usbViewModel.isUsbConnected.value

    // ViewModel que controla el estado del vault
    val vaultViewModel: VaultViewModel = viewModel()

    // Cuando cambia el estado del USB, se vuelve a comprobar el vault
    LaunchedEffect(usbConnected) {
        if (usbConnected) {
            vaultViewModel.checkVault()
        }
    }

    val vaultInitialized = vaultViewModel.vaultInitialized.value

    // Si el vault ya está inicializado, redirige automáticamente a login
    LaunchedEffect(vaultInitialized) {
        if (vaultInitialized) {
            navController.navigate("login") {
                popUpTo("masterPassword") { inclusive = true }
            }
        }
    }

    val context = LocalContext.current

    // Obtiene la ruta actual de navegación
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Controla si se muestra la barra inferior
    val showBottomBar = usbConnected && currentRoute !in listOf(
        "masterPassword",
        "login",
        "recovery"
    )

    Scaffold(
        // Barra superior (idioma + reset)
        topBar = { BarraSuperior(languageViewModel, navController) },

        // Barra inferior (navegación principal)
        bottomBar = {
            if (showBottomBar) {
                BarraInferior(navController)
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            // -------- CASO: USB NO CONECTADO --------
            if (!usbConnected) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {

                        // Mensaje informando que se requiere USB
                        Text(
                            stringResource(id = R.string.usb1),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mensaje adicional de instrucción
                        Text(
                            stringResource(id = R.string.usb2),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

            } else {

                // -------- NAVEGACIÓN PRINCIPAL --------
                NavHost(
                    navController = navController,
                    startDestination = if (vaultInitialized) "login" else "masterPassword"
                ) {

                    // Pantalla de creación de vault
                    composable("masterPassword") {
                        MasterPasswordScreen(navController)
                    }

                    // Pantalla de login
                    composable("login") {
                        LoginScreen(navController)
                    }

                    // Pantalla de recuperación
                    composable("recovery") {
                        RecoveryScreen(navController)
                    }

                    // Pantalla de contraseñas
                    composable("password") {
                        PasswordScreen(navController)
                    }

                    // Pantalla de imágenes
                    composable("image") {
                        ImageScreen(navController)
                    }
                }
            }
        }
    }
}

// HU-4: BARRA SUPERIOR
// Contiene:
// - Nombre y versión de la app
// - Botón de reset del vault
// - Selector de idioma
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(languageViewModel: LanguageViewModel, navController: NavController) {

    val context = LocalContext.current
    val version = remember { getAppVersion(context) }

    // Estado del dropdown de idioma
    var expanded by remember { mutableStateOf(false) }

    // Estado del diálogo de confirmación de reset
    var showDialog by remember { mutableStateOf(false) }

    val currentLanguage = languageViewModel.currentLanguage

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        title = {

            // Título de la app + versión
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

            // Botón reset
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.secondaryContainer
                )
            }

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
                        contentDescription = "Language",
                        modifier = Modifier.size(26.dp),
                        tint = Color.Unspecified
                    )
                }

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

    // Diálogo de confirmación de reset
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },

            // Mensaje principal
            title = {
                Text(stringResource(id = R.string.alert_mensaje),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
            },

            // Pregunta de confirmación
            text = {
                Text(stringResource(id = R.string.alert_pregunta),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
            },

            // Confirmar reset
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        resetVault(context)

                        navController.navigate("masterPassword") {
                            popUpTo(0)
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.alert_boton_confirm),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center)
                }
            },

            // Cancelar
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(id = R.string.alert_boton_cancel),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center)
                }
            }
        )
    }
}

// HU-4: BARRA INFERIOR
// Permite navegar entre:
// - Contraseñas
// - Imágenes
@Composable
fun BarraInferior(navController: NavHostController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {

        // Botón sección contraseñas
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

        // Botón sección imágenes
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

// HU-7 + HU-15: RESET DEL VAULT
// Elimina:
// - Archivos del USB
// - Share almacenada en el dispositivo
// - Preferencias del USB
fun resetVault(context: Context) {

    val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
    val uriString = prefs.getString("usb_uri", null) ?: return
    val treeUri = Uri.parse(uriString)

    val storageManager = UsbStorageManager(context)
    val vaultDir = storageManager.getVaultDirectory(treeUri) ?: return

    // borrar contenido USB
    vaultDir.listFiles().forEach {
        it.delete()
    }

    // borrar share local
    val devicePrefs = context.getSharedPreferences("device_share", Context.MODE_PRIVATE)
    devicePrefs.edit().clear().apply()

    // borrar referencia USB
    prefs.edit().clear().apply()
}