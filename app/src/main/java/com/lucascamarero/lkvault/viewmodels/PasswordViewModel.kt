package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.models.PasswordEntry
import com.lucascamarero.lkvault.security.PasswordRepository
import com.lucascamarero.lkvault.models.EncryptedPasswordEntry

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

    // Estado observable con la lista de contraseñas cifradas
    var passwords = mutableStateOf<List<EncryptedPasswordEntry>>(emptyList())
        private set

    var selectedPassword = mutableStateOf<PasswordEntry?>(null)
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

        // REFRESCAR LISTA AUTOMÁTICAMENTE
        if (result) {
            loadPasswords()
        }
    }

    // Carga todas las contraseñas desde el repositorio
    fun loadPasswords() {
        passwords.value = repository.getAllPasswords()
    }

    fun decryptPassword(entry: EncryptedPasswordEntry, masterKey: ByteArray) {
        selectedPassword.value = repository.decryptPassword(entry, masterKey)
    }

    fun clearSelectedPassword() {
        selectedPassword.value = null
    }

    // Reset estado (opcional)
    fun resetState() {
        creationSuccess.value = null
    }
}