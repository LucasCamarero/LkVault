package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
import com.lucascamarero.lkvault.utils.UsbMonitor

// ViewModel encargado de exponer a la UI el estado del
// dispositivo externo válido (USB con carpeta LkVault).
//
// Forma parte de la arquitectura MVVM:
// - UsbMonitor → Detecta cambios del sistema
// - UsbViewModel → Mantiene estado observable
// - UI (Compose) → Reacciona a los cambios automáticamente
class UsbViewModel(application: Application) : AndroidViewModel(application) {

    // Estado observable por Compose.
    // Cuando cambia su valor, la UI se recompone automáticamente.
    //
    // Solo puede modificarse dentro del ViewModel (private set),
    // evitando que la UI altere el estado directamente.
    var isUsbConnected = mutableStateOf(false)
        private set

    // Instancia de UsbMonitor.
    // Se le pasa el Application context para evitar fugas de memoria.
    // El callback actualiza el estado cada vez que cambia
    // la conexión del dispositivo externo.
    private val monitor = UsbMonitor(application) { connected ->
        isUsbConnected.value = connected
    }

    // Bloque de inicialización.
    // Se ejecuta cuando el ViewModel es creado.
    // Inicia la monitorización del almacenamiento externo.
    init {
        monitor.start()
    }

    // Se ejecuta cuando el ViewModel se destruye
    // (por ejemplo, cuando la pantalla se elimina).
    //
    // Es fundamental detener el monitor para:
    // - Desregistrar el BroadcastReceiver
    // - Evitar fugas de memoria
    // - Evitar escuchar eventos innecesarios
    override fun onCleared() {
        monitor.stop()
        super.onCleared()
    }
}