package com.lucascamarero.lkvault

import com.lucascamarero.lkvault.security.MasterKeyGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MasterKeyGeneratorTest {

    @Test
    fun generatedKeyHasCorrectLength() {

        val generator = MasterKeyGenerator()

        val key = generator.generate()

        // Debe generar exactamente 32 bytes (256 bits)
        assertEquals(32, key.size)
    }

    @Test
    fun generatedKeysAreDifferent() {

        val generator = MasterKeyGenerator()

        val key1 = generator.generate()
        val key2 = generator.generate()

        // Dos claves consecutivas no deberían ser iguales
        assertFalse(key1.contentEquals(key2))
    }
}