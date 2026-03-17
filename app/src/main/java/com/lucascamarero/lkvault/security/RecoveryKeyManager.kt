package com.lucascamarero.lkvault.security

import android.util.Base64

class RecoveryKeyManager {

    fun generateRecoveryKey(shareDevice: ByteArray): String {

        return Base64.encodeToString(
            shareDevice,
            Base64.NO_WRAP
        )
    }

    fun recoverDeviceShare(recoveryKey: String): ByteArray {

        return Base64.decode(
            recoveryKey,
            Base64.NO_WRAP
        )
    }
}