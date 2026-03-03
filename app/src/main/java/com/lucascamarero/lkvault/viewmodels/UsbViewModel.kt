package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.security.VaultManager
import com.lucascamarero.lkvault.utils.UsbMonitor
import com.lucascamarero.lkvault.utils.UsbUtils

// ViewModel encargado de exponer a la UI el estado del
// dispositivo externo válido (USB que contiene la carpeta LkVault).
//
// Forma parte de la arquitectura MVVM:
//
// - UsbMonitor → Detecta cambios físicos del sistema
// - UsbUtils → Valida si el dispositivo cumple los requisitos
// - VaultManager → Gestiona la estructura interna del vault
// - UsbViewModel → Orquesta y mantiene estado observable
// - UI (Compose) → Reacciona automáticamente a los cambios
class UsbViewModel(application: Application) : AndroidViewModel(application) {

    // Estado observable por Compose.
    // true  → Existe USB válido con carpeta LkVault
    // false → No existe USB válido
    var isUsbConnected = mutableStateOf(false)
        private set

    // Gestor de la estructura interna del vault
    private val vaultManager = VaultManager()

    // Instancia de UsbMonitor.
    // Se utiliza el Application context para evitar fugas de memoria.
    private val monitor = UsbMonitor(application) { isValidDevice ->

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

    // Se ejecuta cuando el ViewModel es creado.
    // Inicia la monitorización del almacenamiento externo.
    init {
        monitor.start()
    }

    // Se ejecuta cuando el ViewModel se destruye.
    // Detiene la monitorización para evitar fugas de memoria.
    override fun onCleared() {
        monitor.stop()
        super.onCleared()
    }
}