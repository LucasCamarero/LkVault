package com.lucascamarero.lkvault.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

// HU-6: DETECCIÓN Y VALIDACIÓN DE USB CONECTADO
// Clase encargada de monitorizar en tiempo real  los cambios en USB
class UsbMonitor(

    private val context: Context,

    // Callback que comunica al ViewModel si existe o no un USB
    private val onUsbStateChanged: (Boolean) -> Unit
) {

    // BroadcastReceiver anónimo que escucha eventos del sistema
    // relacionados con el montaje y desmontaje de almacenamiento externo.
    private val receiver = object : BroadcastReceiver() {

        // Se ejecuta automáticamente cuando el sistema
        // emite alguno de los intents registrados en el IntentFilter.
        override fun onReceive(context: Context?, intent: Intent?) {

            // Ante cualquier cambio, delegamos la comprobación
            // a UsbUtils, que aplica el criterio estricto:
            // volumen removible + montado + contiene LkVault.
            val isValid = UsbUtils.isValidExternalDeviceConnected(this@UsbMonitor.context)

            onUsbStateChanged(isValid)
        }
    }

    // Inicia la monitorización.
    // Registra el BroadcastReceiver con los eventos relevantes.
    fun start() {

        val filter = IntentFilter().apply {

            // Se dispara cuando un medio externo se monta (USB insertado)
            addAction(Intent.ACTION_MEDIA_MOUNTED)

            // Se dispara cuando se retira físicamente el medio
            addAction(Intent.ACTION_MEDIA_REMOVED)

            // Se dispara cuando el medio se desmonta lógicamente
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)

            // Necesario para escuchar eventos relacionados con rutas de archivos
            addDataScheme("file")
        }

        context.registerReceiver(receiver, filter)

        // Comprobación inicial:
        // Cuando se inicia la app, verificamos el estado actual
        // sin esperar a que ocurra un evento del sistema.
        val isValid = UsbUtils.isValidExternalDeviceConnected(context)
        onUsbStateChanged(isValid)
    }

    // Detiene la monitorización
    fun stop() {
        context.unregisterReceiver(receiver)
    }
}