package com.lucascamarero.lkvault.security.storage

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.models.docs.EncryptedDocEntry
import com.lucascamarero.lkvault.security.crypto.AesGcmCipher
import com.lucascamarero.lkvault.security.serialization.DocMetadata
import com.lucascamarero.lkvault.security.serialization.DocSerializer
import com.lucascamarero.lkvault.utils.usb.UsbStorageManager
import java.util.UUID

// HU-32: GESTIÓN DE DOCUMENTOS
// Repositorio encargado de gestionar el almacenamiento de documentos cifrados en el USB.
// Implementa el flujo completo:
// - Lectura de documento desde URI
// - Cifrado mediante AES-GCM con la Master Key
// - Persistencia en el vault (archivo binario + metadata JSON)
// - Recuperación y descifrado bajo autenticación activa
class DocRepository(private val context: Context) {

    private val cipher = AesGcmCipher()
    private val storageManager = UsbStorageManager(context)
    private val serializer = DocSerializer()

    // Lee un documento desde un URI, lo cifra con la Master Key y lo almacena en el USB.
    // Se generan dos archivos:
    // - <id>.doc → contenido cifrado
    // - <id>.meta(.json) → metadata (id + nombre)
    fun saveDoc(
        name: String,
        docUri: Uri,
        masterKey: ByteArray
    ): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val docsDir = storageManager.getDocsDirectory(treeUri)
            ?: return false

        val id = UUID.randomUUID().toString()

        val inputBytes = context.contentResolver
            .openInputStream(docUri)
            ?.readBytes() ?: return false

        val encrypted = cipher.encrypt(inputBytes, masterKey)

        // -------- guardar documento cifrado --------
        val docFile = docsDir.createFile(
            "application/octet-stream",
            "$id.doc"
        ) ?: return false

        storageManager.openOutput(docFile.uri)?.use {
            it.write(encrypted)
        }

        // -------- guardar metadata --------
        val meta = DocMetadata(id, name)

        val metaFile = docsDir.createFile(
            "application/json",
            "$id.meta"
        ) ?: return false

        storageManager.openOutput(metaFile.uri)?.use {
            it.write(serializer.metadataToJson(meta).toByteArray())
        }

        // Limpieza del buffer de entrada en memoria
        inputBytes.fill(0)

        return true
    }

    // Recupera todos los documentos almacenados en el vault.
    // Lee los archivos de metadata y asocia cada uno con su archivo cifrado correspondiente.
    fun getAllDocs(): List<EncryptedDocEntry> {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return emptyList()
        val treeUri = Uri.parse(uriString)

        val docsDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("docs") ?: return emptyList()

        val result = mutableListOf<EncryptedDocEntry>()

        docsDir.listFiles().forEach { metaFile ->

            val fileName = metaFile.name ?: return@forEach

            // Se procesan únicamente archivos de metadata
            if (!fileName.endsWith(".meta.json")) return@forEach

            try {
                // -------- leer metadata --------
                val json = context.contentResolver
                    .openInputStream(metaFile.uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: return@forEach

                val meta = serializer.jsonToMetadata(json)

                // -------- buscar documento cifrado --------
                val docFile = docsDir.findFile("${meta.id}.doc")
                    ?: return@forEach

                val encryptedBytes = context.contentResolver
                    .openInputStream(docFile.uri)
                    ?.readBytes()
                    ?: return@forEach

                // -------- añadir a lista --------
                result.add(
                    EncryptedDocEntry(
                        id = meta.id,
                        name = meta.name,
                        encryptedData = encryptedBytes
                    )
                )

            } catch (e: Exception) {
                // Se ignoran entradas corruptas o inconsistentes
            }
        }

        return result
    }

    // Descifra un documento utilizando la Master Key activa.
    // Devuelve los bytes en claro para su uso temporal en la UI.
    fun decryptDoc(
        entry: EncryptedDocEntry,
        masterKey: ByteArray
    ): ByteArray? {

        return try {
            cipher.decrypt(entry.encryptedData, masterKey)
        } catch (e: Exception) {
            null
        }
    }

    // Elimina tanto el archivo cifrado como su metadata asociado.
    fun deleteDoc(id: String): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val docsDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("docs") ?: return false

        // -------- localizar archivos --------
        val docFile = docsDir.findFile("$id.doc")
        val metaFile = docsDir.findFile("$id.meta.json")

        // -------- eliminar --------
        val deletedDoc = docFile?.delete() ?: false
        val deletedMeta = metaFile?.delete() ?: false

        return deletedDoc && deletedMeta
    }

    // Actualiza el nombre de un documento modificando únicamente su metadata.
    fun updateDocName(id: String, newName: String): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val docsDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("docs") ?: return false

        val metaFile = docsDir.findFile("$id.meta.json") ?: return false

        return try {
            val meta = DocMetadata(id, newName)

            storageManager.openOutput(metaFile.uri)?.use {
                it.write(serializer.metadataToJson(meta).toByteArray())
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}