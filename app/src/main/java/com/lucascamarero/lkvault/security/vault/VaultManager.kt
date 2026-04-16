package com.lucascamarero.lkvault.security.vault

import java.io.File

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// Esta clase gestiona la creación y validación de la estructura de carpetas del vault
// dentro del dispositivo USB.
// Su responsabilidad es asegurar que existen los directorios necesarios para almacenar
// los distintos tipos de datos (contraseñas, imágenes, etc.).
class VaultManager {

    private companion object {

        // Carpeta raíz del vault dentro del USB
        const val VAULT_FOLDER = "LkVault"

        // Subcarpeta destinada a almacenar contraseñas cifradas
        const val PASSWORDS_FOLDER = "passwords"

        // Subcarpeta destinada a almacenar imágenes cifradas
        const val IMAGES_FOLDER = "images"

        // Archivo de configuración criptográfica del vault
        const val CONFIG_FILE = "vault.config"

        // Archivo que contiene la Master Key cifrada (envelope encryption)
        const val MASTERKEY_ENC_FILE = "masterkey.enc"

        // Archivo que contiene la share almacenada en el USB (secret splitting)
        const val MASTERKEY_SHARE_FILE = "masterkey.share"
    }

    // Crea la estructura del vault si no existe
    fun createStructureIfNeeded(root: File) {

        // Se construye la ruta al directorio raíz del vault dentro del USB
        val vaultDir = File(root, VAULT_FOLDER)

        // Si la carpeta raíz del vault no existe, no se continúa
        if (!vaultDir.exists()) return

        // Se definen las subcarpetas principales
        val passwordsDir = File(vaultDir, PASSWORDS_FOLDER)
        val imagesDir = File(vaultDir, IMAGES_FOLDER)

        // Si la carpeta de contraseñas no existe, se crea
        if (!passwordsDir.exists()) {
            passwordsDir.mkdirs()
        }

        // Si la carpeta de imágenes no existe, se crea
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
    }
}