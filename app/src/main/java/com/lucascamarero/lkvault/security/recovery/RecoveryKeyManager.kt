package com.lucascamarero.lkvault.security.recovery

import android.util.Base64

// HU-14: GENERACIÓN Y GESTIÓN DE RECOVERY KEY
// Esta clase se encarga de generar y recuperar la Recovery Key del usuario.
// La Recovery Key es una representación en Base64 de una de las partes del esquema
// de secret splitting, permitiendo su almacenamiento externo (por ejemplo, anotación manual).
// Su objetivo es permitir la reconstrucción del secreto y restaurar el acceso
// en caso de pérdida del dispositivo.
class RecoveryKeyManager {

    // Genera una Recovery Key a partir de una share binaria.
    // Convierte la share en una representación Base64 legible.
    fun generateRecoveryKey(
        shareDevice: ByteArray
    ): String {

        // Se codifica la share en Base64 sin saltos de línea
        return Base64.encodeToString(
            shareDevice,
            Base64.NO_WRAP
        )
    }

    // Recupera la share a partir de una Recovery Key.
    // Convierte la cadena Base64 nuevamente a su formato binario original.
    fun recoverDeviceShare(recoveryKey: String): ByteArray {

        // Se decodifica la Recovery Key desde Base64 a bytes
        return Base64.decode(
            recoveryKey,
            Base64.NO_WRAP
        )
    }
}