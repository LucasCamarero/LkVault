package com.lucascamarero.lkvault.security

import android.util.Base64

class RecoveryKeyManager {

    data class RecoveryData(
        val salt: ByteArray,
        val shareDevice: ByteArray,
        val encryptedAux: ByteArray
    )

    fun generateRecoveryKey(
        salt: ByteArray,
        shareDevice: ByteArray,
        encryptedAux: ByteArray
    ): String {

        val combined = salt + shareDevice + encryptedAux

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )
    }

    fun parseRecoveryKey(recoveryKey: String): RecoveryData {

        val data = Base64.decode(recoveryKey, Base64.NO_WRAP)

        val salt = data.copyOfRange(0, 16)
        val shareDevice = data.copyOfRange(16, 48)
        val encryptedAux = data.copyOfRange(48, data.size)

        return RecoveryData(
            salt,
            shareDevice,
            encryptedAux
        )
    }
}