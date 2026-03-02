package com.lucascamarero.lkvault.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

// Clase encargada de monitorizar en tiempo real
// los cambios en dispositivos de almacenamiento externo.
//
// Su responsabilidad NO es decidir si el dispositivo es válido,
// sino detectar cambios del sistema y notificar el nuevo estado.
class UsbMonitor(
    // Contexto necesario para registrar el BroadcastReceiver
    private val context: Context,

    // Callback que comunica a la capa superior (ViewModel)
    // si existe o no un dispositivo externo válido
    private val onUsbStateChanged: (Boolean) -> Unit
) {

    // BroadcastReceiver anónimo que escucha eventos del sistema
    // relacionados con el montaje y desmontaje de almacenamiento externo.
    private val receiver = object : BroadcastReceiver() {

        // Se ejecuta automáticamente cuando el sistema
        // emite alguno de los intents registrados en el IntentFilter.
        override fun onReceive(context: Context?, intent: Intent?) {

            // Cada vez que hay un cambio en el estado del almacenamiento,
            // delegamos la validación real a UsbUtils.
            // El resultado (true/false) se envía al ViewModel mediante el callback.
            onUsbStateChanged(
                UsbUtils.isValidExternalDeviceConnected(this@UsbMonitor.context)
            )
        }
    }

    // Inicia la monitorización.
    // Registra el BroadcastReceiver con los eventos relevantes.
    fun start() {

        // Se construye el filtro de intents que queremos escuchar:
        val filter = IntentFilter().apply {

            // Se dispara cuando un medio externo se monta (USB insertado)
            addAction(Intent.ACTION_MEDIA_MOUNTED)

            // Se dispara cuando se retira físicamente el medio
            addAction(Intent.ACTION_MEDIA_REMOVED)

            // Se dispara cuando el medio se desmonta lógicamente
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)

            // Necesario para que el sistema permita escuchar eventos
            // relacionados con rutas de archivos
            addDataScheme("file")
        }

        // Registro del receiver en el sistema
        context.registerReceiver(receiver, filter)

        // Comprobación inicial:
        // Cuando se inicia la app, verificamos el estado actual
        // sin esperar a que ocurra un evento del sistema.
        onUsbStateChanged(
            UsbUtils.isValidExternalDeviceConnected(context)
        )
    }

    // Detiene la monitorización.
    // Es fundamental llamarlo (por ejemplo, en onCleared del ViewModel)
    // para evitar fugas de memoria y receivers registrados indefinidamente.
    fun stop() {
        context.unregisterReceiver(receiver)
    }
}