package com.lucascamarero.lkvault.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.ui.components.PasswordDialog
import com.lucascamarero.lkvault.utils.password.PasswordGenerator
import com.lucascamarero.lkvault.utils.clipboard.SecureClipboardManager
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

    fun clearFields() {
        name.value = ""
        user.value = ""
        password1.value = ""
        password2.value = ""
    }

    val showMismatch =
        password2.value.isNotEmpty() &&
                password1.value != password2.value

    val isValid =
        password1.value.isNotBlank() &&
                password2.value.isNotBlank() &&
                password1.value == password2.value

    val isValidToSave = isValid && name.value.isNotBlank() && user.value.isNotBlank()

    val isValidToEdit = isValid && user.value.isNotBlank()

    var showUser by remember { mutableStateOf(false) }
    var showPassword1 by remember { mutableStateOf(false) }
    var showPassword2 by remember { mutableStateOf(false) }

    val selectedEncrypted = passwordViewModel.selectedEncrypted.value

    var deleteTarget by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedPassword) {
        selectedPassword?.let {
            user.value = it.username
            password1.value = it.password
            password2.value = it.password
        }
    }

    var showGeneratorDialog by remember { mutableStateOf(false) }

    val letters = remember { mutableStateOf("") }
    val numbers = remember { mutableStateOf("") }
    val symbols = remember { mutableStateOf("") }

    val l = letters.value.toIntOrNull() ?: 0
    val n = numbers.value.toIntOrNull() ?: 0
    val s = symbols.value.toIntOrNull() ?: 0

    val isGeneratorValid =
        l > 0 &&
                (l + n + s) <= 64

    val context = LocalContext.current
    val clipboardManager = remember { SecureClipboardManager(context) }

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
                Spacer(modifier = Modifier.height(200.dp))

                Text(
                    text = stringResource(id = R.string.con_info),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 40.dp),
                    textAlign = TextAlign.Center
                )
            }

        } else {

            items(passwords) { entry ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {

                    // 🔹 Nombre (línea 1)
                    Text(
                        text = entry.name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    // 🔹 Botones (línea 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        IconButton(
                            onClick = {
                                val key = masterKey ?: return@IconButton
                                passwordViewModel.decryptPassword(entry, key)
                            }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }

                        // Copiar usuario
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val key = masterKey ?: return@IconButton

                                val decrypted = passwordViewModel.decryptPasswordDirect(entry, key)

                                decrypted?.let {
                                    clipboardManager.copyWithAutoClear(it.username)
                                }
                                }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copiar usuario",
                                    tint = MaterialTheme.colorScheme.secondaryContainer
                                )
                            }
                            Text(
                                "US",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }

                        // Copiar password
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val key = masterKey ?: return@IconButton

                                val decrypted = passwordViewModel.decryptPasswordDirect(entry, key)

                                decrypted?.let {
                                    clipboardManager.copyWithAutoClear(it.password)
                                }
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copiar contraseña",
                                    tint = MaterialTheme.colorScheme.secondaryContainer
                                )
                            }
                            Text(
                                "PW",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }

                        IconButton(
                            onClick = {
                                deleteTarget = entry.id
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = {
                Text(
                    stringResource(id = R.string.con_delete),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    stringResource(id = R.string.con_confirm_delete),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    passwordViewModel.deletePassword(deleteTarget!!)
                    deleteTarget = null
                }) {
                    Text(
                        stringResource(id = R.string.con_del),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(
                        stringResource(id = R.string.con_cancel_button),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }

    // Dialogo para crear una contraseña
    if (showDialog) {
        PasswordDialog(
            title = stringResource(id = R.string.titulo_alert),
            name = name,
            user = user,
            password1 = password1,
            password2 = password2,
            showUser = showUser,
            onToggleUser = { showUser = !showUser },
            showPassword1 = showPassword1,
            onTogglePassword1 = { showPassword1 = !showPassword1 },
            showPassword2 = showPassword2,
            onTogglePassword2 = { showPassword2 = !showPassword2 },
            showMismatch = showMismatch,
            isValid = isValidToSave && masterKey != null,
            onConfirm = {
                val key = masterKey ?: return@PasswordDialog
                passwordViewModel.createPassword(
                    name.value,
                    user.value,
                    password1.value,
                    key
                )
                clearFields()
                showDialog = false
            },
            onDismiss = {
                clearFields()
                showDialog = false
            },
            onGeneratePassword = {
                showGeneratorDialog = true
            }
        )
    }

    // Dialogo para editar una contraseña
    if (selectedPassword != null) {
        PasswordDialog(
            title = selectedPassword.name,
            name = null,
            user = user,
            password1 = password1,
            password2 = password2,
            showUser = showUser,
            onToggleUser = { showUser = !showUser },
            showPassword1 = showPassword1,
            onTogglePassword1 = { showPassword1 = !showPassword1 },
            showPassword2 = showPassword2,
            onTogglePassword2 = { showPassword2 = !showPassword2 },
            showMismatch = showMismatch,
            isValid = isValidToEdit && masterKey != null,
            onConfirm = {
                val key = masterKey ?: return@PasswordDialog
                val encrypted = selectedEncrypted ?: return@PasswordDialog

                passwordViewModel.updatePassword(
                    entryId = encrypted.id,
                    name = selectedPassword.name, // no editable en UI
                    username = user.value,
                    password = password1.value,
                    masterKey = key
                )

                clearFields()
                passwordViewModel.clearSelectedPassword()
            },
            onDismiss = {
                clearFields()
                passwordViewModel.clearSelectedPassword()
            },
            onGeneratePassword = {
                showGeneratorDialog = true
            }
        )
    }

    // alert dialog para generar automáticamente una contraseña
    if (showGeneratorDialog) {

        AlertDialog(
            onDismissRequest = { showGeneratorDialog = false },

            title = {
                Text(
                    stringResource(id = R.string.con_generation),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                )
            },

            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    OutlinedTextField(
                        value = letters.value,
                        onValueChange = { letters.value = it.filter { c -> c.isDigit() } },
                        label = {
                            Text(
                                stringResource(id = R.string.con_gen_letras),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(26.dp),
                        textStyle = MaterialTheme.typography.labelLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = numbers.value,
                        onValueChange = { numbers.value = it.filter { c -> c.isDigit() } },
                        label = {
                            Text(
                                stringResource(id = R.string.con_gen_numeros),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(26.dp),
                        textStyle = MaterialTheme.typography.labelLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = symbols.value,
                        onValueChange = { symbols.value = it.filter { c -> c.isDigit() } },
                        label = {
                            Text(
                                stringResource(id = R.string.con_gen_simbolos),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(26.dp),
                        textStyle = MaterialTheme.typography.labelLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {

                        val generator = PasswordGenerator()

                        val generated = generator.generate(l, n, s)

                        password1.value = generated
                        password2.value = generated

                        // limpiar estado
                        letters.value = ""
                        numbers.value = ""
                        symbols.value = ""

                        showGeneratorDialog = false
                    },
                    enabled = isGeneratorValid,
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        stringResource(id = R.string.con_gen),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        letters.value = ""
                        numbers.value = ""
                        symbols.value = ""
                        showGeneratorDialog = false
                    },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        stringResource(id = R.string.con_cancel_button),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
    }
}
