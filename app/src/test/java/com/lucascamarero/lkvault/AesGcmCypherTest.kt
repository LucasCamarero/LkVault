package com.lucascamarero.lkvault

import com.lucascamarero.lkvault.security.AesGcmCipher
import com.lucascamarero.lkvault.security.MasterKeyGenerator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import javax.crypto.AEADBadTagException

class AesGcmCipherTest {

    @Test
    fun encryptThenDecryptReturnsOriginalData() {

        val cipher = AesGcmCipher()
        val key = MasterKeyGenerator().generate()

        val data = "Hello Vault".toByteArray()

        val encrypted = cipher.encrypt(data, key)
        val decrypted = cipher.decrypt(encrypted, key)

        // Tras cifrar y descifrar, los datos deben ser exactamente iguales
        assertArrayEquals(data, decrypted)
    }

    @Test
    fun encryptedDataIsDifferentFromPlaintext() {

        val cipher = AesGcmCipher()
        val key = MasterKeyGenerator().generate()

        val data = "Hello Vault".toByteArray()

        val encrypted = cipher.encrypt(data, key)

        // El ciphertext nunca debe coincidir con el plaintext
        assertFalse(data.contentEquals(encrypted))
    }

    @Test(expected = AEADBadTagException::class)
    fun modifiedCiphertextFailsAuthentication() {

        val cipher = AesGcmCipher()
        val key = MasterKeyGenerator().generate()

        val data = "Hello Vault".toByteArray()

        val encrypted = cipher.encrypt(data, key)

        // Simulamos manipulación de datos (ataque o corrupción)
        encrypted[encrypted.size - 1] = (encrypted.last().toInt() xor 1).toByte()

        // Debe lanzar excepción porque el tag GCM no coincide
        cipher.decrypt(encrypted, key)
    }
}