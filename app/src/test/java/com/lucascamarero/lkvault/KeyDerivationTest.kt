package com.lucascamarero.lkvault

import com.lucascamarero.lkvault.security.key.KeyDerivation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class KeyDerivationTest {

    @Test
    fun derivedKeyHasCorrectLength() {

        val kd = KeyDerivation()

        val salt = kd.generateSalt()
        val key = kd.deriveKey("test123".toCharArray(), salt)

        // Debe generar exactamente 32 bytes (256 bits)
        assertEquals(32, key.size)
    }

    @Test
    fun derivedKeyChangesWithDifferentSalt() {

        val kd = KeyDerivation()

        val salt1 = kd.generateSalt()
        val salt2 = kd.generateSalt()

        val key1 = kd.deriveKey("test123".toCharArray(), salt1)
        val key2 = kd.deriveKey("test123".toCharArray(), salt2)

        // Con distinto salt, la clave debe ser distinta
        assertFalse(key1.contentEquals(key2))
    }
}