package com.lucascamarero.lkvault.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DeviceShareStorage(context: Context) {

    private val prefs =
        context.getSharedPreferences("device_share", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ALIAS = "LkVaultDeviceShareKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 128
    }

    private fun getOrCreateKey(): SecretKey {

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)

        return keyGenerator.generateKey()
    }

    fun saveShare(share: ByteArray) {

        val key = getOrCreateKey()

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val encrypted = cipher.doFinal(share)

        val result = iv + encrypted

        val encoded = Base64.encodeToString(result, Base64.NO_WRAP)

        prefs.edit()
            .putString("device_share", encoded)
            .apply()
    }

    fun loadShare(): ByteArray? {

        val encoded = prefs.getString("device_share", null)
            ?: return null

        val data = Base64.decode(encoded, Base64.NO_WRAP)

        val iv = data.copyOfRange(0, IV_LENGTH)
        val ciphertext = data.copyOfRange(IV_LENGTH, data.size)

        val key = getOrCreateKey()

        val cipher = Cipher.getInstance(TRANSFORMATION)

        val spec = GCMParameterSpec(TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return cipher.doFinal(ciphertext)
    }
}