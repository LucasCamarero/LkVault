package com.lucascamarero.lkvault.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

// HU-6: DETECCIÓN Y VALIDACIÓN DE USB CONECTADO
// Esta clase se encarga de monitorizar en tiempo real el estado del almacenamiento externo (USB).
// Escucha eventos del sistema relacionados con el montaje y desmontaje de dispositivos,
// y notifica mediante un callback si existe un USB válido según los criterios definidos.
class UsbMonitor(

    private val context: Context,

    // Callback que informa al ViewModel sobre el estado del USB (válido o no)
    private val onUsbStateChanged: (Boolean) -> Unit
) {

    // BroadcastReceiver que escucha eventos del sistema relacionados con el almacenamiento externo
    private val receiver = object : BroadcastReceiver() {

        // Método invocado automáticamente cuando se recibe un evento registrado
        override fun onReceive(context: Context?, intent: Intent?) {

            // Se evalúa si hay un dispositivo USB válido conectado
            // aplicando los criterios definidos en UsbUtils
            val isValid = UsbUtils.isValidExternalDeviceConnected(this@UsbMonitor.context)

            // Se notifica el resultado al callback
            onUsbStateChanged(isValid)
        }
    }

    // Inicia la monitorización del estado del USB
    fun start() {

        // Se define el filtro de intents que se desean escuchar
        val filter = IntentFilter().apply {

            // Evento cuando un medio externo se monta (USB insertado)
            addAction(Intent.ACTION_MEDIA_MOUNTED)

            // Evento cuando el medio se retira físicamente
            addAction(Intent.ACTION_MEDIA_REMOVED)

            // Evento cuando el medio se desmonta lógicamente
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)

            // Necesario para que los intents incluyan rutas de archivos
            addDataScheme("file")
        }

        // Se registra el BroadcastReceiver con el filtro definido
        context.registerReceiver(receiver, filter)

        // Comprobación inicial del estado del USB
        // Permite conocer el estado actual sin esperar a un evento del sistema
        val isValid = UsbUtils.isValidExternalDeviceConnected(context)

        // Se notifica el estado inicial
        onUsbStateChanged(isValid)
    }

    // Detiene la monitorización del USB
    fun stop() {

        // Se desregistra el BroadcastReceiver para evitar fugas de memoria
        context.unregisterReceiver(receiver)
    }
}