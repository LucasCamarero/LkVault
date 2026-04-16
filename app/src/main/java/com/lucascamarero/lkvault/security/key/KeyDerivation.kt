package com.lucascamarero.lkvault.security.key

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom

// HU-10: IMPLEMENTACIÓN DE DERIVACIÓN DE CLAVE CON ARGON2ID
class KeyDerivation {

    private companion object {

        // Número de iteraciones del algoritmo Argon2.
        // Incrementa el coste computacional del cálculo de la clave derivada
        const val ITERATIONS = 3

        // Memoria utilizada por Argon2 en kilobytes.
        // 65536 KB = 64 MB, lo que dificulta ataques mediante GPU
        const val MEMORY_KB = 65536

        // Nivel de paralelismo del algoritmo.
        // Define el número de hilos utilizados durante la derivación
        const val PARALLELISM = 1

        // Longitud de la clave derivada en bytes.
        // 32 bytes = 256 bits, tamaño adecuado para AES-256
        const val OUTPUT_LENGTH = 32

        // Tamaño del salt aleatorio utilizado en la derivación
        const val SALT_LENGTH = 16
    }

    // Generador de números aleatorios criptográficamente seguro
    private val secureRandom = SecureRandom()

    // Genera un salt aleatorio de 16 bytes que se utilizará en Argon2.
    // El salt evita que la misma contraseña produzca siempre la misma clave
    fun generateSalt(): ByteArray {

        // Se crea el array de bytes con el tamaño definido para el salt
        val salt = ByteArray(SALT_LENGTH)

        // Se rellena con valores aleatorios seguros
        secureRandom.nextBytes(salt)

        // Se devuelve el salt generado
        return salt
    }

    // Deriva una clave criptográfica a partir de la contraseña maestra del usuario
    // y un salt aleatorio previamente generado
    fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {

        // Construcción de los parámetros de configuración de Argon2id
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withIterations(ITERATIONS)
            .withMemoryAsKB(MEMORY_KB)
            .withParallelism(PARALLELISM)
            .build()

        // Generador de bytes basado en Argon2
        val generator = Argon2BytesGenerator()

        // Inicialización del generador con los parámetros configurados
        generator.init(params)

        // Array que contendrá la clave derivada final
        val result = ByteArray(OUTPUT_LENGTH)

        // Ejecución del algoritmo Argon2 para generar la clave
        generator.generateBytes(password, result)

        // Se devuelve la clave derivada
        return result
    }
}