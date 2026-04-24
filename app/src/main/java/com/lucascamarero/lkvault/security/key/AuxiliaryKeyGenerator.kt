package com.lucascamarero.lkvault.security.key

import java.security.SecureRandom

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
// Esta clase es responsable de generar una clave auxiliar aleatoria.
// La clave auxiliar se utiliza dentro del esquema de secret splitting
// para proteger indirectamente la Master Key del vault.
class AuxiliaryKeyGenerator {

    companion object {

        // Tamaño de la clave auxiliar en bytes.
        const val KEY_SIZE = 32
    }

    // Generador de números aleatorios criptográficamente seguro.
    private val secureRandom = SecureRandom()

    // Genera una nueva clave auxiliar aleatoria.
    // Esta clave posteriormente será dividida en dos partes mediante
    // el mecanismo de secret splitting (2-de-2).
    fun generate(): ByteArray {

        // Se crea un array de bytes del tamaño definido para la clave.
        val key = ByteArray(KEY_SIZE)

        // Se rellenan los bytes con valores aleatorios seguros.
        secureRandom.nextBytes(key)

        // Se devuelve la clave auxiliar generada.
        return key
    }
}
