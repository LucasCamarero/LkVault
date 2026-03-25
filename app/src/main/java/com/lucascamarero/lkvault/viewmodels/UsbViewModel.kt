package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.security.VaultManager
import com.lucascamarero.lkvault.utils.UsbMonitor
import com.lucascamarero.lkvault.utils.UsbUtils

// HU-6: DETECCIÓN Y VALIDACIÓN DE USB CONECTADO
// Este ViewModel gestiona el estado del dispositivo USB dentro de la aplicación.
// Se encarga de monitorizar la conexión/desconexión, validar el dispositivo
// y asegurar que la estructura del vault esté correctamente creada.
// Expone un estado observable para que la UI reaccione a cambios en tiempo real.
class UsbViewModel(application: Application) : AndroidViewModel(application) {

    // Estado observable que indica si hay un USB válido conectado (con carpeta LkVault)
    var isUsbConnected = mutableStateOf(false)
        private set

    // Gestor encargado de asegurar la estructura interna del vault en el USB
    private val vaultManager = VaultManager()

    // Monitor que escucha cambios en el estado del almacenamiento externo
    // Se utiliza Application context para evitar fugas de memoria
    private val monitor = UsbMonitor(application) { isValidDevice ->

        // Si se detecta un dispositivo USB válido
        if (isValidDevice) {

            // Se obtiene la raíz del volumen válido
            val root = UsbUtils.getValidExternalRoot(application)

            if (root != null) {
                // Se asegura que la estructura interna del vault esté creada
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

    // Método llamado cuando el ViewModel se destruye
    override fun onCleared() {

        // Se detiene la monitorización para evitar fugas de memoria
        monitor.stop()

        // Se invoca la implementación base
        super.onCleared()
    }
}