package com.lucascamarero.lkvault.security.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

// HU-9: IMPLEMENTACIÓN DE CIFRADO AES-256-GCM
// Responsable de cifrar y descifrar datos con confidencialidad e integridad autenticada.
class AesGcmCipher {

    private companion object {
        // Algoritmo utilizado
        const val TRANSFORMATION = "AES/GCM/NoPadding"

        // Tamaño del IV recomendado para GCM
        const val IV_LENGTH = 12

        // Tamaño del tag de autenticación
        const val TAG_LENGTH = 128
    }

    // Generador de números aleatorios criptográficamente seguro
    private val secureRandom = SecureRandom()

    // Cifra datos utilizando AES-256-GCM.
    fun encrypt(data: ByteArray, key: ByteArray): ByteArray {

        // Se crea un vector de inicialización.
        val iv = ByteArray(IV_LENGTH)

        // Se rellena el IV con bytes aleatorios criptográficamente seguros.
        secureRandom.nextBytes(iv)

        // Se obtiene una instancia del motor criptográfico.
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Se crea una especificación de clave AES a partir del array de bytes recibido.
        val keySpec = SecretKeySpec(key, "AES")

        // Se construye la especificación de parámetros para GCM.
        val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)

        // Se inicializa el cipher en modo cifrado (ENCRYPT_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        // Se ejecuta el proceso de cifrado sobre los datos.
        val ciphertext = cipher.doFinal(data)

        // Se construye el bloque final concatenando:
        // - el IV (necesario para el descifrado posterior)
        // - el ciphertext que ya incluye el tag de autenticación
        return iv + ciphertext
    }

    // Descifra datos previamente cifrados con AES-256-GCM.
    fun decrypt(encryptedData: ByteArray, key: ByteArray): ByteArray {

        // Se extrae el IV (vector de inicialización) del inicio del bloque cifrado.
        val iv = encryptedData.copyOfRange(0, IV_LENGTH)

        // Se extrae el resto del bloque cifrado, que contiene:
        // - el ciphertext
        // - el tag de autenticación GCM (incluido al final del ciphertext).
        val ciphertext = encryptedData.copyOfRange(IV_LENGTH, encryptedData.size)

        // Se obtiene una instancia del motor criptográfico configurado.
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Se crea una especificación de clave AES a partir del array de bytes recibido.
        val keySpec = SecretKeySpec(key, "AES")

        // Se construye la especificación de parámetros GCM
        val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)

        // Se inicializa el cipher en modo descifrado (DECRYPT_MODE)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        // Se ejecuta el proceso de descifrado sobre el ciphertext.
        // Durante esta operación, GCM también verifica el tag de autenticación.
        // Si los datos han sido modificados o la clave es incorrecta,
        // se lanzará una excepción de autenticación.
        return cipher.doFinal(ciphertext)
    }
}