package com.lucascamarero.lkvault.security

import java.io.File

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// Clase encargada de gestionar la estructura interna del USB
// - NO gestiona aún elementos criptográficos (HU8).
class VaultManager {

    private companion object {

        // Nombre de la carpeta raíz del vault
        const val VAULT_FOLDER = "LkVault"

        // Subcarpeta donde se almacenarán las contraseñas cifradas
        const val PASSWORDS_FOLDER = "passwords"

        // Subcarpeta donde se almacenarán las imágenes cifradas
        const val IMAGES_FOLDER = "images"
    }

    // Crea las subcarpetas internas necesarias si no existen
    // (excepto LkVault que tiene que estar creada)
    fun createStructureIfNeeded(root: File) {

        val vaultDir = File(root, VAULT_FOLDER)

        // Si no existe la carpeta raíz, no hacemos nada.
        if (!vaultDir.exists()) return

        val passwordsDir = File(vaultDir, PASSWORDS_FOLDER)
        val imagesDir = File(vaultDir, IMAGES_FOLDER)

        // Crear carpeta passwords si no existe
        if (!passwordsDir.exists()) {
            passwordsDir.mkdir()
        }

        // Crear carpeta images si no existe
        if (!imagesDir.exists()) {
            imagesDir.mkdir()
        }
    }

    /*
    // Valida que la estructura mínima del vault sea correcta.
    //
    // Para considerarse válida debe existir:
    // - Carpeta LkVault
    // - Subcarpeta passwords
    // - Subcarpeta images
    //
    // Devuelve true si la estructura es válida.
    fun validateStructure(root: File): Boolean {

        val vaultDir = File(root, VAULT_FOLDER)

        if (!vaultDir.exists() || !vaultDir.isDirectory) return false

        val passwordsDir = File(vaultDir, PASSWORDS_FOLDER)
        val imagesDir = File(vaultDir, IMAGES_FOLDER)

        return passwordsDir.exists() &&
                passwordsDir.isDirectory &&
                imagesDir.exists() &&
                imagesDir.isDirectory
    }
    */
}