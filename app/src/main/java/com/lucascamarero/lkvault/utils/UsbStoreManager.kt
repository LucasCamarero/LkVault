package com.lucascamarero.lkvault.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStream

// HU-7: ESTRUCTURA INTERNA DE ALMACENAMIENTO EN USB
// Clase encargada de gestionar operaciones de lectura y escritura
// en el dispositivo USB utilizando el Storage Access Framework (SAF).
class UsbStorageManager(private val context: Context) {

    // Devuelve el directorio LkVault dentro del árbol concedido por SAF
    fun getVaultDirectory(treeUri: Uri): DocumentFile? {

        // Se obtiene el directorio raíz asociado al URI concedido por SAF
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return null

        // Dentro de esa raíz se busca la carpeta "LkVault"
        return root.findFile("LkVault")
    }

    // Comprueba si existe un archivo en LkVault
    fun fileExists(treeUri: Uri, name: String): Boolean {

        // Se obtiene el directorio del vault; si no existe se devuelve false
        val dir = getVaultDirectory(treeUri) ?: return false

        // Se comprueba si dentro del directorio existe el archivo indicado
        return dir.findFile(name) != null
    }

    // Crea un archivo dentro de LkVault
    fun createFile(treeUri: Uri, name: String): Uri? {

        // Se obtiene el directorio LkVault; si no existe se cancela la operación
        val dir = getVaultDirectory(treeUri) ?: return null

        // Se comprueba si el archivo ya existe dentro del vault
        val existing = dir.findFile(name)

        // Si el archivo ya existe, se devuelve directamente su URI
        if (existing != null) return existing.uri

        // Si no existe, se crea un nuevo archivo binario con el nombre indicado
        val file = dir.createFile("application/octet-stream", name)

        // Se devuelve la URI del archivo creado (o null si falló la creación)
        return file?.uri
    }

    // Abre un flujo de escritura hacia un archivo identificado por su URI
    fun openOutput(uri: Uri): OutputStream? {

        // Se solicita al ContentResolver un OutputStream asociado al URI
        return context.contentResolver.openOutputStream(uri)
    }
}