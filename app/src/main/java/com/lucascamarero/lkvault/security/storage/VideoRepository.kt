package com.lucascamarero.lkvault.security.storage

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.models.videos.EncryptedVideoEntry
import com.lucascamarero.lkvault.security.crypto.AesGcmCipher
import com.lucascamarero.lkvault.security.serialization.VideoMetadata
import com.lucascamarero.lkvault.security.serialization.VideoSerializer
import com.lucascamarero.lkvault.utils.usb.UsbStorageManager
import java.util.UUID

// HU-31: GESTIÓN DE ARCHIVOS DE VIDEO
// Repositorio encargado de gestionar el almacenamiento de vídeos cifrados en el USB.
// Implementa el flujo completo:
// - Lectura de vídeo desde URI
// - Cifrado mediante AES-GCM con la Master Key
// - Persistencia en el vault (archivo binario + metadata JSON)
// - Recuperación y descifrado bajo autenticación activa
class VideoRepository(private val context: Context) {

    private val cipher = AesGcmCipher()
    private val storageManager = UsbStorageManager(context)
    private val serializer = VideoSerializer()

    // Lee un vídeo desde un URI, lo cifra con la Master Key y lo almacena en el USB.
    // Se generan dos archivos:
    // - <id>.vid → contenido cifrado
    // - <id>.meta(.json) → metadata (id + nombre)
    fun saveVideo(
        name: String,
        videoUri: Uri,
        masterKey: ByteArray
    ): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val videosDir = storageManager.getVideosDirectory(treeUri)
            ?: return false

        val id = UUID.randomUUID().toString()

        val inputBytes = context.contentResolver
            .openInputStream(videoUri)
            ?.readBytes() ?: return false

        val encrypted = cipher.encrypt(inputBytes, masterKey)

        // -------- guardar vídeo cifrado --------
        val audioFile = videosDir.createFile(
            "application/octet-stream",
            "$id.vid"
        ) ?: return false

        storageManager.openOutput(audioFile.uri)?.use {
            it.write(encrypted)
        }

        // -------- guardar metadata --------
        val meta = VideoMetadata(id, name)

        val metaFile = videosDir.createFile(
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

    // Recupera todos los vídeos almacenados en el vault.
    // Lee los archivos de metadata y asocia cada uno con su archivo cifrado correspondiente.
    fun getAllVideos(): List<EncryptedVideoEntry> {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return emptyList()
        val treeUri = Uri.parse(uriString)

        val videosDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("videos") ?: return emptyList()

        val result = mutableListOf<EncryptedVideoEntry>()

        videosDir.listFiles().forEach { metaFile ->

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

                // -------- buscar vídeo cifrado --------
                val videoFile = videosDir.findFile("${meta.id}.vid")
                    ?: return@forEach

                val encryptedBytes = context.contentResolver
                    .openInputStream(videoFile.uri)
                    ?.readBytes()
                    ?: return@forEach

                // -------- añadir a lista --------
                result.add(
                    EncryptedVideoEntry(
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

    // Descifra un vídeo utilizando la Master Key activa.
    // Devuelve los bytes en claro para su uso temporal en la UI.
    fun decryptVideo(
        entry: EncryptedVideoEntry,
        masterKey: ByteArray
    ): ByteArray? {

        return try {
            cipher.decrypt(entry.encryptedData, masterKey)
        } catch (e: Exception) {
            null
        }
    }

    // Elimina tanto el archivo cifrado como su metadata asociado.
    fun deleteVideo(id: String): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val videosDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("videos") ?: return false

        // -------- localizar archivos --------
        val vidFile = videosDir.findFile("$id.vid")
        val metaFile = videosDir.findFile("$id.meta.json")

        // -------- eliminar --------
        val deletedVid = vidFile?.delete() ?: false
        val deletedMeta = metaFile?.delete() ?: false

        return deletedVid && deletedMeta
    }

    // Actualiza el nombre de un vídeo modificando únicamente su metadata.
    fun updateVideoName(id: String, newName: String): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val videosDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("videos") ?: return false

        val metaFile = videosDir.findFile("$id.meta.json") ?: return false

        return try {
            val meta = VideoMetadata(id, newName)

            storageManager.openOutput(metaFile.uri)?.use {
                it.write(serializer.metadataToJson(meta).toByteArray())
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}