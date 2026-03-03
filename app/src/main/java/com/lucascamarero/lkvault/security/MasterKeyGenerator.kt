package com.lucascamarero.lkvault.security

import java.security.SecureRandom

// Responsable exclusivo de generar la Master Key
// de 256 bits utilizada para cifrar todos los datos del vault.
//
// Esta clase NO:
// - Persiste la clave
// - La cifra
// - La divide
// - La convierte a String
//
// Solo la genera de forma criptográficamente segura.
class MasterKeyGenerator {

    private companion object {
        const val KEY_SIZE_BYTES = 32 // 256 bits
    }

    private val secureRandom = SecureRandom()

    // Genera una Master Key aleatoria de 256 bits.
    fun generate(): ByteArray {

        val key = ByteArray(KEY_SIZE_BYTES)

        secureRandom.nextBytes(key)

        return key
    }
}