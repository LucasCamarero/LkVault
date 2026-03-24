package com.lucascamarero.lkvault.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.security.VaultRecoveryManager

@Composable
fun RecoveryScreen(navController: NavController) {

    val context = LocalContext.current
    val recoveryManager = VaultRecoveryManager(context)

    var recoveryKey by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            stringResource(id = R.string.intro_recovery),
            color = MaterialTheme.colorScheme.primaryContainer,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = recoveryKey,
            onValueChange = { recoveryKey = it },
            label = {
                Text(
                    stringResource(id = R.string.rk),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.labelLarge
                )
            },
            textStyle = MaterialTheme.typography.labelLarge,
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

                val masterKey = recoveryManager.recoverVault(recoveryKey)

                if (masterKey != null) {

                    error = false

                    navController.navigate("password") {
                        popUpTo("login") { inclusive = true }
                    }

                } else {
                    error = true
                }
            },
            enabled = recoveryKey.isNotBlank(),
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
                stringResource(id = R.string.recuperar),
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (error) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                stringResource(id = R.string.validez_rv),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}