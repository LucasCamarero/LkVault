package com.lucascamarero.lkvault.security.key

import java.security.SecureRandom

// HU-8: GENERACIÓN DE MASTER KEY ALEATORIA
// Responsable de generar la Master Key de 256 bits utilizada para cifrar todos los datos del vault.
class MasterKeyGenerator {

    private companion object {

        // Tamaño de la clave en bytes. 32 bytes = 256 bits, tamaño requerido para AES-256.
        const val KEY_SIZE_BYTES = 32
    }

    // Generador de números aleatorios criptográficamente seguro
    private val secureRandom = SecureRandom()

    // Genera una Master Key aleatoria de 256 bits.
    fun generate(): ByteArray {

        // Se crea un array de bytes con el tamaño definido para la clave
        val key = ByteArray(KEY_SIZE_BYTES)

        // Se rellenan los bytes con valores aleatorios seguros
        secureRandom.nextBytes(key)

        // Se devuelve la Master Key generada
        return key
    }
}
