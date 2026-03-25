package com.lucascamarero.lkvault.security

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// Representa la configuración criptográfica del vault almacenada en el archivo vault.config.
// Este archivo contiene únicamente metadatos necesarios para derivar la clave desde la contraseña.
data class VaultConfig(

    // Versión del formato del archivo de configuración.
    val version: Int,

    // Salt utilizado por Argon2 para derivar la clave desde la contraseña del usuario.
    val salt: ByteArray,

    // Número de iteraciones del algoritmo Argon2.
    val iterations: Int,

    // Cantidad de memoria utilizada por Argon2 en kilobytes.
    val memoryKB: Int,

    // Nivel de paralelismo (número de hilos) utilizado por Argon2.
    val parallelism: Int
) {

    companion object {

        // Versión actual del formato de configuración del vault.
        private const val CURRENT_VERSION = 1

        // Longitud esperada del salt en bytes.
        private const val SALT_LENGTH = 16

        // Crea una nueva configuración del vault usando los parámetros actuales del sistema.
        fun create(salt: ByteArray): VaultConfig {

            // Verificación de seguridad: el salt debe tener exactamente 16 bytes.
            if (salt.size != SALT_LENGTH) {
                throw IllegalArgumentException("El Salt debe tener 16 bytes")
            }

            // Se devuelve una nueva instancia de VaultConfig con los parámetros definidos.
            return VaultConfig(
                version = CURRENT_VERSION,
                salt = salt,
                iterations = 3,
                memoryKB = 65536,
                parallelism = 1
            )
        }

        // Carga la configuración del vault desde el archivo vault.config.
        fun load(file: File): VaultConfig {

            // Se abre el archivo en modo lectura binaria.
            DataInputStream(FileInputStream(file)).use { input ->

                // Se lee la versión del formato almacenada en el archivo.
                val version = input.readUnsignedByte()

                // Se crea un array de bytes para almacenar el salt leído.
                val salt = ByteArray(SALT_LENGTH)

                // Se leen exactamente 16 bytes desde el archivo para rellenar el salt.
                input.readFully(salt)

                // Se leen los parámetros de Argon2 almacenados en el archivo.
                val iterations = input.readInt()
                val memoryKB = input.readInt()
                val parallelism = input.readUnsignedByte()

                // Se construye y devuelve el objeto VaultConfig con los datos leídos.
                return VaultConfig(
                    version,
                    salt,
                    iterations,
                    memoryKB,
                    parallelism
                )
            }
        }
    }

    // Guarda la configuración actual en el archivo vault.config.
    fun save(file: File) {

        // Se abre el archivo en modo escritura binaria.
        DataOutputStream(FileOutputStream(file)).use { output ->

            // Se escribe la versión del formato.
            output.writeByte(version)

            // Se escriben los 16 bytes del salt.
            output.write(salt)

            // Se escriben los parámetros del algoritmo Argon2.
            output.writeInt(iterations)
            output.writeInt(memoryKB)

            // Se escribe el nivel de paralelismo.
            output.writeByte(parallelism)
        }
    }
}