package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.utils.UsbStorageManager

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// ViewModel encargado de gestionar el estado del vault (si está inicializado o no)
class VaultViewModel(application: Application) : AndroidViewModel(application) {

    // Estado observable que indica si el vault ya ha sido inicializado.
    var vaultInitialized = mutableStateOf(false)
        private set

    // Instancia del gestor de almacenamiento USB utilizada para comprobar archivos dentro del vault
    private val storageManager = UsbStorageManager(application)

    init {
        checkVault()
    }

    // Comprueba si el vault está ya inicializado
    fun checkVault() {

        // Obtiene las preferencias compartidas donde se guarda la URI del USB
        val prefs = getApplication<Application>()
            .getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)

        // Recupera la URI del USB almacenada previamente (si existe)
        val uriString = prefs.getString("usb_uri", null)

        // Si existe una URI guardada, significa que el usuario ya concedió permisos al USB
        if (uriString != null) {

            // Se convierte la cadena almacenada en un objeto Uri válido
            val uri = Uri.parse(uriString)

            // Se comprueba si dentro del directorio LkVault existe el archivo vault.config
            // Si existe, significa que el vault ya ha sido inicializado
            vaultInitialized.value =
                storageManager.fileExists(uri, "vault.config")
        }
    }
}