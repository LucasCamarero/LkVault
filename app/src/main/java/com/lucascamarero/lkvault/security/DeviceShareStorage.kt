package com.lucascamarero.lkvault.security

import android.content.Context
import android.util.Base64

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
// Esta clase se encarga de almacenar y recuperar la parte del secreto
// que reside en el dispositivo móvil dentro del esquema de secret splitting.
// Esta share se guarda en almacenamiento interno mediante SharedPreferences.
class DeviceShareStorage(context: Context) {

    // Acceso a las SharedPreferences privadas de la aplicación.
    // MODE_PRIVATE garantiza que solo esta aplicación pueda acceder a estos datos.
    private val prefs =
        context.getSharedPreferences("device_share", Context.MODE_PRIVATE)

    // Guarda la share correspondiente al dispositivo.
    fun saveShare(share: ByteArray) {

        // Codificación del array de bytes a una cadena Base64.
        val encoded = Base64.encodeToString(share, Base64.NO_WRAP)

        // Se almacena la cadena codificada bajo la clave "device_share".
        prefs.edit()
            .putString("device_share", encoded)
            .apply()
    }

    // Recupera la share almacenada en el dispositivo.
    // Si no existe una share almacenada se devuelve null.
    fun loadShare(): ByteArray? {

        // Se obtiene la cadena Base64 almacenada.
        val encoded = prefs.getString("device_share", null)
            ?: return null

        // Se decodifica la cadena Base64 nuevamente a un array de bytes.
        return Base64.decode(encoded, Base64.NO_WRAP)
    }
}