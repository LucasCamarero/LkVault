package com.lucascamarero.lkvault

import com.lucascamarero.lkvault.security.MasterKeyGenerator
import com.lucascamarero.lkvault.security.MasterKeyProtector
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import javax.crypto.AEADBadTagException

class MasterKeyProtectorTest {

    @Test
    fun masterKeyCanBeProtectedAndRecovered() {

        val generator = MasterKeyGenerator()
        val protector = MasterKeyProtector()

        val masterKey = generator.generate()
        val derivedKey = generator.generate() // solo para test

        val encrypted = protector.protect(masterKey, derivedKey)
        val recovered = protector.recover(encrypted, derivedKey)

        // La clave recuperada debe ser exactamente igual a la original
        assertArrayEquals(masterKey, recovered)
    }

    @Test
    fun protectedMasterKeyIsDifferentFromOriginal() {

        val generator = MasterKeyGenerator()
        val protector = MasterKeyProtector()

        val masterKey = generator.generate()
        val derivedKey = generator.generate()

        val encrypted = protector.protect(masterKey, derivedKey)

        // El bloque cifrado no debe coincidir con la clave original
        assertFalse(masterKey.contentEquals(encrypted))
    }

    @Test(expected = AEADBadTagException::class)
    fun recoverFailsWithWrongKey() {

        val generator = MasterKeyGenerator()
        val protector = MasterKeyProtector()

        val masterKey = generator.generate()
        val correctKey = generator.generate()
        val wrongKey = generator.generate()

        val encrypted = protector.protect(masterKey, correctKey)

        // Intentar descifrar con clave incorrecta debe fallar
        protector.recover(encrypted, wrongKey)
    }
}