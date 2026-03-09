package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.security.VaultManager
import com.lucascamarero.lkvault.utils.UsbMonitor
import com.lucascamarero.lkvault.utils.UsbUtils

// HU-6: DETECCIÓN Y VALIDACIÓN DE USB CONECTADO
// ViewModel encargado del USB que contiene la carpeta LkVault
class UsbViewModel(application: Application) : AndroidViewModel(application) {

    // Estado observable por Compose de USB válido con carpeta LkVault
    var isUsbConnected = mutableStateOf(false)
        private set

    // Gestor de la estructura interna del vault
    private val vaultManager = VaultManager()

    // Instancia de UsbMonitor. Se utiliza el Application context para evitar fugas de memoria.
    private val monitor = UsbMonitor(application) { isValidDevice ->

        // Si existe un dispositivo USB
        if (isValidDevice) {

            // Obtenemos la raíz del volumen válido
            val root = UsbUtils.getValidExternalRoot(application)

            if (root != null) {
                // Si existe LkVault, aseguramos que las subcarpetas necesarias existan
                vaultManager.createStructureIfNeeded(root)
            }
        }

        // Se actualiza el estado observable para la UI
        isUsbConnected.value = isValidDevice
    }

    // Inicialización del ViewModel
    init {
        // Inicia la monitorización del almacenamiento externo
        monitor.start()
    }

    // Detiene la monitorización para evitar fugas de memoria.
    override fun onCleared() {
        monitor.stop()
        // Se ejecuta cuando el ViewModel se destruye.
        super.onCleared()
    }
}