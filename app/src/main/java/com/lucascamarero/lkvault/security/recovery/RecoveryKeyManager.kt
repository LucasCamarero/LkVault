package com.lucascamarero.lkvault.security.recovery

import android.util.Base64

// HU-14: GENERACIÓN Y GESTIÓN DE RECOVERY KEY
// Esta clase se encarga de generar y recuperar la Recovery Key del usuario.
// La Recovery Key representa la share del dispositivo codificada en Base64,
// permitiendo su almacenamiento o anotación por parte del usuario para recuperación futura.
// Su objetivo es permitir restaurar el acceso en caso de pérdida del dispositivo.
class RecoveryKeyManager {

    // Genera una Recovery Key a partir de la share del dispositivo.
    // Convierte la share binaria en una representación Base64 legible.
    fun generateRecoveryKey(
        shareDevice: ByteArray
    ): String {

        // Se codifica la share del dispositivo en Base64 sin saltos de línea
        return Base64.encodeToString(
            shareDevice,
            Base64.NO_WRAP
        )
    }

    // Recupera la share del dispositivo a partir de una Recovery Key.
    // Convierte la cadena Base64 nuevamente a su formato binario original.
    fun recoverDeviceShare(recoveryKey: String): ByteArray {

        // Se decodifica la Recovery Key desde Base64 a bytes
        return Base64.decode(
            recoveryKey,
            Base64.NO_WRAP
        )
    }
}