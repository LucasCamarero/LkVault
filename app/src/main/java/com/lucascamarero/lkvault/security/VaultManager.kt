package com.lucascamarero.lkvault.security

import java.io.File

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// Gestiona la estructura del vault dentro del dispositivo USB.
class VaultManager {

    private companion object {

        // Carpeta raíz del vault
        const val VAULT_FOLDER = "LkVault"

        // Subcarpetas de datos
        const val PASSWORDS_FOLDER = "passwords"
        const val IMAGES_FOLDER = "images"

        // Archivos de configuración y claves
        const val CONFIG_FILE = "vault.config"
        const val MASTERKEY_ENC_FILE = "masterkey.enc"
        const val MASTERKEY_SHARE_FILE = "masterkey.share"
    }

    // Crea la estructura del vault si no existe
    fun createStructureIfNeeded(root: File) {

        val vaultDir = File(root, VAULT_FOLDER)

        // Si no existe la carpeta raíz del vault, no hacemos nada
        if (!vaultDir.exists()) return

        val passwordsDir = File(vaultDir, PASSWORDS_FOLDER)
        val imagesDir = File(vaultDir, IMAGES_FOLDER)

        val configFile = File(vaultDir, CONFIG_FILE)
        val masterKeyEncFile = File(vaultDir, MASTERKEY_ENC_FILE)
        val masterKeyShareFile = File(vaultDir, MASTERKEY_SHARE_FILE)

        // Crear carpeta passwords si no existe
        if (!passwordsDir.exists()) {
            passwordsDir.mkdir()
        }

        // Crear carpeta images si no existe
        if (!imagesDir.exists()) {
            imagesDir.mkdir()
        }

        // Crear archivo vault.config si no existe
        if (!configFile.exists()) {
            configFile.createNewFile()
        }

        // Crear archivo masterkey.enc si no existe
        if (!masterKeyEncFile.exists()) {
            masterKeyEncFile.createNewFile()
        }

        // Crear archivo masterkey.share si no existe
        if (!masterKeyShareFile.exists()) {
            masterKeyShareFile.createNewFile()
        }
    }
}