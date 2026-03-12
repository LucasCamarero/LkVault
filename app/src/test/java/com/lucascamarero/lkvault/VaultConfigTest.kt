package com.lucascamarero.lkvault

import com.lucascamarero.lkvault.security.KeyDerivation
import com.lucascamarero.lkvault.security.VaultConfig
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class VaultConfigTest {

    @Test
    fun saveAndLoadPreservesData() {

        val keyDerivation = KeyDerivation()
        val salt = keyDerivation.generateSalt()

        val config = VaultConfig.create(salt)

        val file = File.createTempFile("vault", ".config")

        config.save(file)

        val loaded = VaultConfig.load(file)

        assertEquals(config.version, loaded.version)
        assertArrayEquals(config.salt, loaded.salt)
        assertEquals(config.iterations, loaded.iterations)
        assertEquals(config.memoryKB, loaded.memoryKB)
        assertEquals(config.parallelism, loaded.parallelism)
    }
}