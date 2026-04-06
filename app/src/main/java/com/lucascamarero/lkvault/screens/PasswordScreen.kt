package com.lucascamarero.lkvault.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.viewmodels.PasswordViewModel
import com.lucascamarero.lkvault.viewmodels.SessionViewModel

// HU-20: CREACIÓN Y ALMACENAMIENTO DE CONTRASEÑAS
// Pantalla de gestión de contraseñas
@Composable
fun PasswordScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel
) {

    val passwordViewModel: PasswordViewModel = viewModel()

    val masterKey = sessionViewModel.masterKey

    val selectedPassword = passwordViewModel.selectedPassword.value

    // 🔥 NUEVO: estado lista
    val passwords = passwordViewModel.passwords.value

    // 🔥 NUEVO: cargar al entrar
    LaunchedEffect(Unit) {
        passwordViewModel.loadPasswords()
    }

    var showDialog by remember { mutableStateOf(false) }

    val name = remember { mutableStateOf("") }
    val user = remember { mutableStateOf("") }
    val password1 = remember { mutableStateOf("") }
    val password2 = remember { mutableStateOf("") }

    val showMismatch =
        password2.value.isNotEmpty() &&
                password1.value != password2.value

    val isValid =
        password1.value.isNotBlank() &&
                password2.value.isNotBlank() &&
                password1.value == password2.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 20.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        // -------- HEADER --------
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.titulo_con),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.titleSmall
                )

                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir contraseña",
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // -------- LISTA / VACÍO --------
        if (passwords.isEmpty()) {

            item {
                Text(
                    text = "Sin contraseñas todavía",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 40.dp)
                )
            }

        } else {

            items(passwords) { entry ->

                Text(
                    text = entry.name,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .clickable {

                            val key = masterKey ?: return@clickable

                            passwordViewModel.decryptPassword(entry, key)
                        }
                )
            }
        }
    }

    // -------- DIALOG --------
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    stringResource(id = R.string.titulo_alert),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    OutlinedTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        label = {
                            Text(stringResource(id = R.string.con_name))
                        }
                    )

                    OutlinedTextField(
                        value = user.value,
                        onValueChange = { user.value = it },
                        label = {
                            Text(stringResource(id = R.string.con_user))
                        }
                    )

                    OutlinedTextField(
                        value = password1.value,
                        onValueChange = { password1.value = it },
                        label = {
                            Text(stringResource(id = R.string.con_password))
                        },
                        visualTransformation = PasswordVisualTransformation()
                    )

                    if (showMismatch) {
                        Text(
                            text = stringResource(id = R.string.error_maestra),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    OutlinedTextField(
                        value = password2.value,
                        onValueChange = { password2.value = it },
                        label = {
                            Text(stringResource(id = R.string.con_confirm))
                        },
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {

                        val key = masterKey ?: return@TextButton

                        passwordViewModel.createPassword(
                            name = name.value,
                            username = user.value,
                            password = password1.value,
                            masterKey = key
                        )

                        showDialog = false

                        name.value = ""
                        user.value = ""
                        password1.value = ""
                        password2.value = ""
                    },
                    enabled = isValid && masterKey != null
                ) {
                    Text(stringResource(id = R.string.con_save_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(id = R.string.con_cancel_button))
                }
            }
        )
    }

    if (selectedPassword != null) {

        AlertDialog(
            onDismissRequest = {
                passwordViewModel.clearSelectedPassword()
            },
            title = {
                Text(
                    text = selectedPassword.name,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column {

                    Text("Usuario: ${selectedPassword.username}")
                    Text("Contraseña: ${selectedPassword.password}")

                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        passwordViewModel.clearSelectedPassword()
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}