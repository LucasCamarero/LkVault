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

@Composable
fun ScreenManager(languageViewModel: LanguageViewModel) {

    val navController = rememberNavController()

    val usbViewModel: UsbViewModel = viewModel()
    val usbConnected = usbViewModel.isUsbConnected.value

    val vaultViewModel: VaultViewModel = viewModel()

    LaunchedEffect(usbConnected) {
        if (usbConnected) {
            vaultViewModel.checkVault()
        }
    }

    val vaultInitialized = vaultViewModel.vaultInitialized.value

    LaunchedEffect(vaultInitialized) {
        if (vaultInitialized) {
            navController.navigate("login") {
                popUpTo("masterPassword") { inclusive = true }
            }
        }
    }

    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = usbConnected && currentRoute !in listOf(
        "masterPassword",
        "login",
        "recovery"
    )

    Scaffold(
        topBar = { BarraSuperior(languageViewModel, navController) },
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

            if (!usbConnected) {

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

            } else {

                NavHost(
                    navController = navController,
                    startDestination = if (vaultInitialized) "login" else "masterPassword"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(languageViewModel: LanguageViewModel, navController: NavController) {

    val context = LocalContext.current
    val version = remember { getAppVersion(context) }

    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

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

            // 🔴 BOTÓN RESET
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            // 🔴 SELECTOR IDIOMA
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
        }
    )

    // 🔴 DIÁLOGO DE CONFIRMACIÓN
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(stringResource(id = R.string.alert_mensaje),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
            },
            text = {
                Text(stringResource(id = R.string.alert_pregunta),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
            },
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

@Composable
fun BarraInferior(navController: NavHostController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {

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

fun resetVault(context: Context) {

    val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
    val uriString = prefs.getString("usb_uri", null) ?: return
    val treeUri = Uri.parse(uriString)

    val storageManager = UsbStorageManager(context)
    val vaultDir = storageManager.getVaultDirectory(treeUri) ?: return

    // 🔴 borrar contenido USB
    vaultDir.listFiles().forEach {
        it.delete()
    }

    // 🔴 borrar share local
    val devicePrefs = context.getSharedPreferences("device_share", Context.MODE_PRIVATE)
    devicePrefs.edit().clear().apply()

    // 🔴 borrar referencia USB
    prefs.edit().clear().apply()
}