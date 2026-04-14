package com.lucascamarero.lkvault.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lucascamarero.lkvault.R

@Composable
fun PasswordDialog(
    title: String,
    name: MutableState<String>? = null,
    user: MutableState<String>,
    password1: MutableState<String>,
    password2: MutableState<String>,
    showUser: Boolean,
    onToggleUser: () -> Unit,
    showPassword1: Boolean,
    onTogglePassword1: () -> Unit,
    showPassword2: Boolean,
    onTogglePassword2: () -> Unit,
    showMismatch: Boolean,
    isValid: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onGeneratePassword: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                textAlign = TextAlign.Center
            )
        },

        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Nombre (solo en crear)
                name?.let {
                    OutlinedTextField(
                        value = it.value,
                        onValueChange = { v -> it.value = v.uppercase() },
                        label = {
                            Text(
                                stringResource(id = R.string.con_name),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = MaterialTheme.typography.labelLarge
                            )},
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

                // Usuario
                OutlinedTextField(
                    value = user.value,
                    onValueChange = { user.value = it },
                    label = { Text(stringResource(id = R.string.con_user),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    ) },
                    visualTransformation = if (showUser)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onToggleUser) {
                            Icon(
                                imageVector = if (showUser)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
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

                // Password 1
                OutlinedTextField(
                    value = password1.value,
                    onValueChange = { password1.value = it },
                    label = { Text(stringResource(id = R.string.con_password),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    ) },
                    visualTransformation = if (showPassword1)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = onTogglePassword1) {
                                Icon(
                                    imageVector = if (showPassword1)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }

                            IconButton(onClick = { onGeneratePassword() }) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Generar contraseña"
                                )
                            }
                        }
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

                // Error
                if (showMismatch) {
                    Text(
                        text = stringResource(id = R.string.con_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }

                // Password 2
                OutlinedTextField(
                    value = password2.value,
                    onValueChange = { password2.value = it },
                    label = { Text(stringResource(id = R.string.con_confirm),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    ) },
                    visualTransformation = if (showPassword2)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onTogglePassword2) {
                            Icon(
                                imageVector = if (showPassword2)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
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
                onClick = onConfirm,
                enabled = isValid,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                Text(stringResource(id = R.string.con_save_button),
                    style = MaterialTheme.typography.bodySmall)
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                ))
            {
                Text(stringResource(id = R.string.con_cancel_button),
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}