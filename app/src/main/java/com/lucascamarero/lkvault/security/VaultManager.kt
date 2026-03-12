package com.lucascamarero.lkvault.security

import java.io.File
import java.io.FileOutputStream
import android.util.Log

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
        if (!vaultDir.exists()) return

        val passwordsDir = File(vaultDir, PASSWORDS_FOLDER)
        val imagesDir = File(vaultDir, IMAGES_FOLDER)

        if (!passwordsDir.exists()) {
            passwordsDir.mkdirs()
        }

        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
    }
}