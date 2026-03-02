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
import com.lucascamarero.lkvault.screens.PasswordScreen
import com.lucascamarero.lkvault.ui.theme.Typography2
import com.lucascamarero.lkvault.viewmodels.LanguageViewModel
import com.lucascamarero.lkvault.viewmodels.UsbViewModel
import com.lucascamarero.lkvault.R

@Composable
fun ScreenManager(languageViewModel: LanguageViewModel) {

    val navController = rememberNavController()

    val usbViewModel: UsbViewModel = viewModel()
    val usbConnected = usbViewModel.isUsbConnected.value

    Scaffold(
        topBar = { BarraSuperior(languageViewModel) },
        bottomBar = {
            if (usbConnected) {
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

                // Pantalla bloqueada si no hay USB
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
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            stringResource(id = R.string.usb2),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

            } else {

                // Navegación normal si USB está conectado
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
                selectedIconColor = MaterialTheme.colorScheme.background,
                unselectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
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
                selectedIconColor = MaterialTheme.colorScheme.background,
                unselectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
                indicatorColor = Color.Transparent
            )
        )
    }
}