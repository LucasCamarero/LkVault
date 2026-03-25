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

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
// HU-13: INTEGRACIÓN CON ANDROID KEYSTORE
// Esta clase es responsable de almacenar de forma segura la share del dispositivo
// (device share) dentro del almacenamiento local.
// La share se cifra utilizando una clave protegida por Android Keystore,
// evitando que pueda ser extraída en texto plano incluso si el almacenamiento es comprometido.
class DeviceShareStorage(context: Context) {

    // SharedPreferences utilizado para persistir la share cifrada en el dispositivo
    private val prefs =
        context.getSharedPreferences("device_share", Context.MODE_PRIVATE)

    companion object {
        // Alias de la clave almacenada en el Keystore
        private const val KEY_ALIAS = "LkVaultDeviceShareKey"

        // Proveedor del Keystore de Android
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"

        // Transformación criptográfica utilizada (AES-GCM)
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        // Longitud del IV para GCM
        private const val IV_LENGTH = 12

        // Longitud del tag de autenticación
        private const val TAG_LENGTH = 128
    }

    // Obtiene la clave del Keystore o la crea si no existe
    private fun getOrCreateKey(): SecretKey {

        // Se accede al Keystore de Android
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        // Se intenta recuperar una clave existente
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey

        // Si existe, se devuelve directamente
        if (existing != null) return existing

        // Se crea un generador de claves AES dentro del Keystore
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        // Se define la configuración de la clave
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            // La clave se podrá usar para cifrado y descifrado
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            // Se usa GCM como modo de operación
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)

            // No se utiliza padding
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)

            // Tamaño de clave de 256 bits
            .setKeySize(256)
            .build()

        // Se inicializa el generador con la configuración
        keyGenerator.init(spec)

        // Se genera y devuelve la clave protegida en el Keystore
        return keyGenerator.generateKey()
    }

    // Guarda la share del dispositivo cifrada en almacenamiento local
    fun saveShare(share: ByteArray) {

        // Se obtiene la clave protegida del Keystore
        val key = getOrCreateKey()

        // Se obtiene una instancia del cipher AES-GCM
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Se inicializa en modo cifrado
        cipher.init(Cipher.ENCRYPT_MODE, key)

        // Se obtiene el IV generado automáticamente
        val iv = cipher.iv

        // Se cifra la share
        val encrypted = cipher.doFinal(share)

        // Se concatena IV + ciphertext (incluye tag)
        val result = iv + encrypted

        // Se codifica en Base64 para almacenamiento seguro en texto
        val encoded = Base64.encodeToString(result, Base64.NO_WRAP)

        // Se guarda en SharedPreferences
        prefs.edit()
            .putString("device_share", encoded)
            .apply()
    }

    // Recupera y descifra la share del dispositivo almacenada
    fun loadShare(): ByteArray? {

        // Se obtiene el valor almacenado en Base64
        val encoded = prefs.getString("device_share", null)
            ?: return null

        // Se decodifica el contenido
        val data = Base64.decode(encoded, Base64.NO_WRAP)

        // Se extrae el IV del inicio
        val iv = data.copyOfRange(0, IV_LENGTH)

        // Se extrae el ciphertext + tag
        val ciphertext = data.copyOfRange(IV_LENGTH, data.size)

        // Se obtiene la clave del Keystore
        val key = getOrCreateKey()

        // Se obtiene una instancia del cipher AES-GCM
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Se construye la especificación GCM con el IV
        val spec = GCMParameterSpec(TAG_LENGTH, iv)

        // Se inicializa en modo descifrado
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        // Se descifra la share
        // Si los datos han sido alterados, se lanzará una excepción
        return cipher.doFinal(ciphertext)
    }
}