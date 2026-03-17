package com.lucascamarero.lkvault.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

// HU13: INTEGRACIÓN CON ANDROID KEYSTORE
class AndroidKeystoreManager {

    companion object {
        private const val KEY_ALIAS = "LkVaultDeviceShareKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    fun getOrCreateKey(): SecretKey {

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existingKey =
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey

        if (existingKey != null) return existingKey

        val keyGenerator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)

        return keyGenerator.generateKey()
    }
}