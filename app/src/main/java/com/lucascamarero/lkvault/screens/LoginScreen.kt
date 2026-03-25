package com.lucascamarero.lkvault.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.security.SecurityManager
import com.lucascamarero.lkvault.security.VaultUnlockManager

@Composable
fun LoginScreen(
    navController: NavController
) {

    val context = LocalContext.current
    val securityManager = remember { SecurityManager(context) }

    val password = remember { mutableStateOf("") }

    var error by remember { mutableStateOf(false) }

    var attemptsLeft by remember { mutableStateOf(securityManager.getAttemptsLeft()) }
    var isBlocked by remember { mutableStateOf(securityManager.isBlocked()) }
    var blockTime by remember { mutableStateOf(securityManager.getRemainingBlockTime()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {

            Text(
                stringResource(id = R.string.login),
                color = MaterialTheme.colorScheme.primaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = {
                    Text(
                        stringResource(id = R.string.maestra),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                textStyle = MaterialTheme.typography.labelLarge,
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {

                    if (securityManager.isBlocked()) {
                        isBlocked = true
                        blockTime = securityManager.getRemainingBlockTime()
                        return@Button
                    }

                    val unlockManager = VaultUnlockManager(context)
                    val masterKey = unlockManager.unlockVault(password.value)

                    if (masterKey != null) {

                        securityManager.registerSuccess()

                        error = false
                        attemptsLeft = securityManager.getAttemptsLeft()
                        isBlocked = false

                        navController.navigate("password") {
                            popUpTo("login") { inclusive = true }
                        }

                    } else {

                        securityManager.registerFailure()

                        error = true
                        attemptsLeft = securityManager.getAttemptsLeft()
                        isBlocked = securityManager.isBlocked()
                        blockTime = securityManager.getRemainingBlockTime()
                    }
                },
                enabled = password.value.isNotBlank() && !isBlocked,
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
                    stringResource(id = R.string.boton_login),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // -------- ERROR --------
            if (error && !isBlocked) {
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = stringResource(id = R.string.error_login) + " $attemptsLeft",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // -------- BLOQUEO --------
            if (isBlocked) {
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = stringResource(id = R.string.bloqueo) + " ${blockTime / 1000}s",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}