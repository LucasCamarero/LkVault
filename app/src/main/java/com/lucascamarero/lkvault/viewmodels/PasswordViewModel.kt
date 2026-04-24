package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.models.passwords.PasswordEntry
import com.lucascamarero.lkvault.security.storage.PasswordRepository
import com.lucascamarero.lkvault.models.passwords.EncryptedPasswordEntry

// HU-20: CREAR CONTRASEÑA
// HU-21: EDITAR CONTRASEÑA
// HU-22: ELIMINAR CONTRASEÑA
// HU-23: VISUALIZAR CONTRASEÑA BAJO AUTENTICACIÓN
// ViewModel encargado de conectar la UI con la capa de persistencia de contraseñas.
// Gestiona el flujo completo:
// - Recepción de datos desde la UI
// - Delegación en PasswordRepository
// - Exposición de estados observables para la UI (lista, selección, resultados)
class PasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PasswordRepository(application)

    // Estado que indica si la creación de una contraseña ha sido exitosa
    var creationSuccess = mutableStateOf<Boolean?>(null)
        private set

    // Estado observable con la lista de contraseñas cifradas
    var passwords = mutableStateOf<List<EncryptedPasswordEntry>>(emptyList())
        private set

    // Contraseña actualmente seleccionada (en claro)
    var selectedPassword = mutableStateOf<PasswordEntry?>(null)
        private set

    // Resultado de operación de actualización
    var updateSuccess = mutableStateOf<Boolean?>(null)
        private set

    // Resultado de operación de eliminación
    var deleteSuccess = mutableStateOf<Boolean?>(null)
        private set

    // Entrada cifrada seleccionada (útil para referencia en UI)
    var selectedEncrypted = mutableStateOf<EncryptedPasswordEntry?>(null)
        private set

    // Crea una nueva contraseña y la almacena en el vault
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

        // Si la operación ha sido exitosa, se refresca la lista
        if (result) {
            loadPasswords()
        }
    }

    // Actualiza una contraseña existente
    fun updatePassword(
        entryId: String,
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

        val result = repository.updatePassword(entryId, entry, masterKey)

        updateSuccess.value = result

        if (result) {
            loadPasswords()
        }
    }

    // Elimina una contraseña del vault
    fun deletePassword(entryId: String) {

        val result = repository.deletePassword(entryId)

        deleteSuccess.value = result

        if (result) {
            loadPasswords()
        }
    }

    // Carga todas las contraseñas desde el repositorio (sin descifrar)
    fun loadPasswords() {
        passwords.value = repository.getAllPasswords()
    }

    // Descifra una contraseña y la expone para la UI
    fun decryptPassword(entry: EncryptedPasswordEntry, masterKey: ByteArray) {

        selectedEncrypted.value = entry

        selectedPassword.value = repository.decryptPassword(entry, masterKey)
    }

    // Descifrado directo sin modificar estado interno (uso puntual)
    fun decryptPasswordDirect(
        entry: EncryptedPasswordEntry,
        masterKey: ByteArray
    ): PasswordEntry? {
        return repository.decryptPasswordDirect(entry, masterKey)
    }

    // Limpia la selección actual
    fun clearSelectedPassword() {
        selectedPassword.value = null
        selectedEncrypted.value = null
    }

    // Resetea estados de operación (útil para feedback UI)
    fun resetState() {
        creationSuccess.value = null
    }
}