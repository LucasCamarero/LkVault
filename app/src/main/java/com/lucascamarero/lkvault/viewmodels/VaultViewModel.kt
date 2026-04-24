package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.utils.usb.UsbStorageManager

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// HU-15: FLUJO COMPLETO DE INICIALIZACIÓN CRIPTOGRÁFICA (parcial)
// Este ViewModel gestiona el estado del vault en la aplicación.
// Su responsabilidad es comprobar si el vault ha sido previamente inicializado,
// utilizando como criterio la existencia del archivo "vault.config" en el USB.
// Expone un estado observable que permite a la UI decidir el flujo (setup o acceso).
class VaultViewModel(application: Application) : AndroidViewModel(application) {

    // Estado observable que indica si el vault está inicializado
    // (basado en la existencia del archivo "vault.config")
    var vaultInitialized = mutableStateOf(false)
        private set

    // Gestor de acceso al almacenamiento USB mediante SAF
    private val storageManager = UsbStorageManager(application)

    // Inicialización del ViewModel
    init {
        // Se realiza una comprobación inicial del estado del vault
        checkVault()
    }

    // Comprueba si el vault ya ha sido inicializado
    // verificando la existencia del archivo "vault.config"
    fun checkVault() {

        // Se accede a las SharedPreferences donde se almacena la URI del USB
        val prefs = getApplication<Application>()
            .getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)

        // Se recupera la URI del USB (si el usuario ya concedió permisos previamente)
        val uriString = prefs.getString("usb_uri", null)

        // Si existe una URI, se puede acceder al USB
        if (uriString != null) {

            // Se convierte la cadena en un objeto Uri válido
            val uri = Uri.parse(uriString)

            // Se comprueba si existe el archivo "vault.config" dentro de LkVault.
            // La presencia de este archivo se utiliza como indicador de inicialización del vault.
            vaultInitialized.value =
                storageManager.fileExists(uri, "vault.config")
        }
    }
}