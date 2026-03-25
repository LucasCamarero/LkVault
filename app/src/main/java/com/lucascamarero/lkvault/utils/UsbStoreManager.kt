package com.lucascamarero.lkvault.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStream

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// Esta clase encapsula las operaciones de acceso al almacenamiento externo (USB)
// utilizando el Storage Access Framework (SAF).
// Permite localizar el directorio del vault, comprobar la existencia de archivos,
// crearlos y obtener flujos de escritura seguros mediante URIs.
class UsbStorageManager(private val context: Context) {

    // Devuelve el directorio "LkVault" dentro del árbol concedido por SAF
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

    // Crea un archivo dentro del directorio del vault
    fun createFile(treeUri: Uri, name: String): Uri? {

        // Se obtiene el directorio del vault; si no existe, se aborta la operación
        val dir = getVaultDirectory(treeUri) ?: return null

        // Se comprueba si el archivo ya existe
        val existing = dir.findFile(name)

        // Si ya existe, se reutiliza devolviendo su URI
        if (existing != null) return existing.uri

        // Si no existe, se crea un nuevo archivo binario (octet-stream)
        val file = dir.createFile("application/octet-stream", name)

        // Se devuelve la URI del archivo creado (o null si falla)
        return file?.uri
    }

    // Abre un flujo de salida para escribir datos en un archivo identificado por su URI
    fun openOutput(uri: Uri): OutputStream? {

        // Se obtiene un OutputStream a través del ContentResolver
        return context.contentResolver.openOutputStream(uri)
    }
}