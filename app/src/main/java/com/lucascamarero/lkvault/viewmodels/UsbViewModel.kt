package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.security.vault.VaultManager
import com.lucascamarero.lkvault.utils.usb.UsbMonitor
import com.lucascamarero.lkvault.utils.usb.UsbUtils

// HU-6: DETECCIÓN Y VALIDACIÓN DE USB CONECTADO
// Este ViewModel gestiona el estado del dispositivo USB dentro de la aplicación.
// Se encarga de monitorizar la conexión/desconexión, validar el dispositivo
// (según los criterios definidos en UsbUtils) y, en caso de ser válido,
// delegar la creación de la estructura del vault.
// Expone un estado observable para que la UI reaccione a cambios en tiempo real.
class UsbViewModel(application: Application) : AndroidViewModel(application) {

    // Estado observable que indica si hay un USB válido conectado
    // (es decir, un volumen accesible que contiene la carpeta LkVault)
    var isUsbConnected = mutableStateOf(false)
        private set

    // Gestor encargado de garantizar la existencia de la estructura interna del vault
    private val vaultManager = VaultManager()

    // Monitor que escucha cambios en el estado del almacenamiento externo
    // Se utiliza Application context para evitar fugas de memoria
    private val monitor = UsbMonitor(application) { isValidDevice ->

        // Si se detecta un dispositivo USB válido según UsbUtils
        if (isValidDevice) {

            // Se obtiene la raíz del volumen válido
            val root = UsbUtils.getValidExternalRoot(application)

            if (root != null) {
                // Se delega en VaultManager la creación de la estructura interna del vault si es necesario
                vaultManager.createStructureIfNeeded(root)
            }
        }

        // Se actualiza el estado observable que consume la UI
        isUsbConnected.value = isValidDevice
    }

    // Inicialización del ViewModel
    init {
        // Se inicia la monitorización del USB al crear el ViewModel
        monitor.start()
    }

    // Detiene la monitorización
    override fun onCleared() {

        // Se detiene la monitorización para evitar fugas de memoria
        monitor.stop()

        // Se invoca la implementación base
        super.onCleared()
    }
}