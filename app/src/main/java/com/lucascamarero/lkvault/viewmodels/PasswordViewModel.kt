package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.models.PasswordEntry
import com.lucascamarero.lkvault.security.PasswordRepository

// HU-20: CREAR CONTRASEÑA (VIEWMODEL)
// Este ViewModel conecta la UI con el repositorio.
// Se encarga de:
// - Recibir datos desde la UI
// - Delegar en el repositorio
// - Gestionar estado de resultado
class PasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PasswordRepository(application)

    // Estado de creación (puedes usarlo luego para feedback UI)
    var creationSuccess = mutableStateOf<Boolean?>(null)
        private set

    // Crear contraseña
    fun createPassword(
        name: String,
        username: String,
        password: String,
        masterKey: ByteArray
    ) {

        val entry = PasswordEntry(
            name = name,
            username = username,
            password = password
        )

        val result = repository.createPassword(entry, masterKey)

        creationSuccess.value = result
    }

    // Reset estado (opcional)
    fun resetState() {
        creationSuccess.value = null
    }
}