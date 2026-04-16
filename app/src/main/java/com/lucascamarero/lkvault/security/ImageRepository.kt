package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.models.EncryptedImageEntry
import com.lucascamarero.lkvault.utils.UsbStorageManager
import java.util.UUID

class ImageRepository(private val context: Context) {

    private val cipher = AesGcmCipher()
    private val storageManager = UsbStorageManager(context)
    private val serializer = ImageSerializer()

    // -------------------------
    // HU27 - GUARDAR IMAGEN
    // -------------------------
    fun saveImage(
        name: String,
        imageUri: Uri,
        masterKey: ByteArray
    ): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val imagesDir = storageManager.getImagesDirectory(treeUri)
            ?: return false

        val id = UUID.randomUUID().toString()

        val inputBytes = context.contentResolver
            .openInputStream(imageUri)
            ?.readBytes() ?: return false

        val encrypted = cipher.encrypt(inputBytes, masterKey)

        // -------- guardar imagen cifrada --------
        val imageFile = imagesDir.createFile(
            "application/octet-stream",
            "$id.img"
        ) ?: return false

        storageManager.openOutput(imageFile.uri)?.use {
            it.write(encrypted)
        }

        // -------- guardar metadata --------
        val meta = ImageMetadata(id, name)

        val metaFile = imagesDir.createFile(
            "application/json",
            "$id.meta"
        ) ?: return false

        storageManager.openOutput(metaFile.uri)?.use {
            it.write(serializer.metadataToJson(meta).toByteArray())
        }

        inputBytes.fill(0)

        return true
    }

    // -------------------------
    // HU28 - LISTAR IMÁGENES
    // -------------------------
    fun getAllImages(): List<EncryptedImageEntry> {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return emptyList()
        val treeUri = Uri.parse(uriString)

        val imagesDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("images") ?: return emptyList()

        val result = mutableListOf<EncryptedImageEntry>()

        imagesDir.listFiles().forEach { metaFile ->

            val fileName = metaFile.name ?: return@forEach

            // 🔥 IMPORTANTE: tus archivos son .meta.json
            if (!fileName.endsWith(".meta.json")) return@forEach

            try {
                // -------- leer metadata --------
                val json = context.contentResolver
                    .openInputStream(metaFile.uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: return@forEach

                val meta = serializer.jsonToMetadata(json)

                // -------- buscar imagen cifrada --------
                val imageFile = imagesDir.findFile("${meta.id}.img")
                    ?: return@forEach

                val encryptedBytes = context.contentResolver
                    .openInputStream(imageFile.uri)
                    ?.readBytes()
                    ?: return@forEach

                // -------- añadir a lista --------
                result.add(
                    EncryptedImageEntry(
                        id = meta.id,
                        name = meta.name,
                        encryptedData = encryptedBytes
                    )
                )

            } catch (e: Exception) {
                // opcional: Log.e("ImageRepo", "Error leyendo imagen", e)
            }
        }

        return result
    }

    // -------------------------
    // HU28 - DESCIFRAR
    // -------------------------
    fun decryptImage(
        entry: EncryptedImageEntry,
        masterKey: ByteArray
    ): ByteArray? {

        return try {
            cipher.decrypt(entry.encryptedData, masterKey)
        } catch (e: Exception) {
            null
        }
    }

    // -------------------------
    // HU29 - ELIMINAR
    // -------------------------
    fun deleteImage(id: String): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val imagesDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("images") ?: return false

        // -------- localizar archivos --------
        val imgFile = imagesDir.findFile("$id.img")
        val metaFile = imagesDir.findFile("$id.meta.json")

        // -------- eliminar --------
        val deletedImg = imgFile?.delete() ?: false
        val deletedMeta = metaFile?.delete() ?: false

        return deletedImg && deletedMeta
    }

    fun updateImageName(id: String, newName: String): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val imagesDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("images") ?: return false

        val metaFile = imagesDir.findFile("$id.meta.json") ?: return false

        return try {
            val meta = ImageMetadata(id, newName)

            storageManager.openOutput(metaFile.uri)?.use {
                it.write(serializer.metadataToJson(meta).toByteArray())
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}