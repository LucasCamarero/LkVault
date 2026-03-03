package com.lucascamarero.lkvault.security

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom

class KeyDerivation {

    private companion object {
        const val ITERATIONS = 3
        const val MEMORY_KB = 65536
        const val PARALLELISM = 1
        const val OUTPUT_LENGTH = 32
        const val SALT_LENGTH = 16
    }

    private val secureRandom = SecureRandom()

    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return salt
    }

    fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {

        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withIterations(ITERATIONS)
            .withMemoryAsKB(MEMORY_KB)
            .withParallelism(PARALLELISM)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(params)

        val result = ByteArray(OUTPUT_LENGTH)

        generator.generateBytes(password, result)

        return result
    }
}