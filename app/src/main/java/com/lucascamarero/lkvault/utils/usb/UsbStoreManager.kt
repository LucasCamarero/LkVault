package com.lucascamarero.lkvault.utils.usb

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStream

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// Gestiona el acceso al almacenamiento externo (USB) mediante el Storage Access Framework (SAF).
// Permite localizar y operar sobre la estructura interna del vault (LkVault), incluyendo
// la gestión de archivos y subdirectorios definidos por el sistema.
class UsbStorageManager(private val context: Context) {

    // Devuelve el directorio raíz "LkVault" dentro del almacenamiento seleccionado
    fun getVaultDirectory(treeUri: Uri): DocumentFile? {

        // Se obtiene el directorio raíz a partir del URI proporcionado por SAF
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return null

        // Se busca dentro de la raíz la carpeta "LkVault"
        return root.findFile("LkVault")
    }

    // Comprueba si existe un archivo dentro del directorio del vault
    fun fileExists(treeUri: Uri, name: String): Boolean {

        // Se obtiene el directorio del vault; si no existe, no puede haber archivos
        val dir = getVaultDirectory(treeUri) ?: return false

        // Se comprueba si el archivo con el nombre indicado existe
        return dir.findFile(name) != null
    }

    // Crea (o reutiliza si ya existe) un archivo dentro del directorio del vault
    fun createFile(treeUri: Uri, name: String): Uri? {

        // Se obtiene el directorio del vault; si no existe, se aborta la operación
        val dir = getVaultDirectory(treeUri) ?: return null

        // Se comprueba si el archivo ya existe
        val existing = dir.findFile(name)

        // Si ya existe, se reutiliza devolviendo su URI
        if (existing != null) return existing.uri

        // Si no existe, se crea un nuevo archivo binario (application/octet-stream)
        val file = dir.createFile("application/octet-stream", name)

        // Se devuelve la URI del archivo creado (o null si falla)
        return file?.uri
    }

    // Abre un flujo de salida para escribir datos en un archivo identificado por su URI
    // Se utiliza el modo "wt" (write + truncate), que sobrescribe completamente el contenido existente
    fun openOutput(uri: Uri): OutputStream? {
        return context.contentResolver.openOutputStream(uri, "wt")
    }

    // Devuelve el subdirectorio "passwords" dentro del vault
    fun getPasswordsDirectory(treeUri: Uri): DocumentFile? {

        val vaultDir = getVaultDirectory(treeUri) ?: return null

        return vaultDir.findFile("passwords")
    }

    // Devuelve el subdirectorio "images" dentro del vault
    fun getImagesDirectory(treeUri: Uri): DocumentFile? {

        val vaultDir = getVaultDirectory(treeUri) ?: return null

        return vaultDir.findFile("images")
    }

    // Devuelve el subdirectorio "audios" dentro del vault
    fun getAudiosDirectory(treeUri: Uri): DocumentFile? {

        val vaultDir = getVaultDirectory(treeUri) ?: return null

        return vaultDir.findFile("audios")
    }

    // Crea las carpetas si estas no existen
    fun ensureVaultStructure(treeUri: Uri) {

        val vaultDir = getVaultDirectory(treeUri) ?: return

        if (vaultDir.findFile("passwords") == null) {
            vaultDir.createDirectory("passwords")
        }

        if (vaultDir.findFile("images") == null) {
            vaultDir.createDirectory("images")
        }

        if (vaultDir.findFile("audios") == null) {
            vaultDir.createDirectory("audios")
        }
    }
}