package com.lucascamarero.lkvault.security.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

// HU-13: INTEGRACIÓN CON ANDROID KEYSTORE
// Esta clase gestiona la creación y recuperación de una clave simétrica almacenada
// de forma segura en el Android Keystore.
// Su objetivo es proteger la share local del dispositivo dentro del esquema de secret splitting,
// garantizando que esta clave no sea exportable fuera del entorno seguro del sistema.
class AndroidKeystoreManager {

    companion object {
        // Alias único utilizado para identificar la clave dentro del Keystore
        private const val KEY_ALIAS = "LkVaultDeviceShareKey"

        // Nombre del proveedor del Keystore de Android
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    // Obtiene la clave almacenada en el Keystore si ya existe,
    // o la genera en caso contrario.
    fun getOrCreateKey(): SecretKey {

        // Se obtiene una instancia del Keystore de Android
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)

        // Se inicializa el Keystore
        keyStore.load(null)

        // Se intenta recuperar una clave existente mediante su alias
        val existingKey =
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey

        // Si la clave ya existe, se devuelve directamente
        if (existingKey != null) return existingKey

        // Se crea un generador de claves AES dentro del Keystore
        val keyGenerator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

        // Se define la configuración de la clave a generar
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            // La clave podrá usarse tanto para cifrar como para descifrar
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            // Se especifica el modo de bloque GCM (necesario para AES-GCM)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)

            // No se utiliza padding (requerido por GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)

            // Tamaño de clave: 256 bits
            .setKeySize(256)

            // Se construye la especificación final
            .build()

        // Se inicializa el generador con la configuración definida
        keyGenerator.init(spec)

        // Se genera y devuelve la nueva clave almacenada en el Keystore
        return keyGenerator.generateKey()
    }
}
