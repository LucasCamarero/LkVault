package com.lucascamarero.lkvault.security.storage

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.models.images.EncryptedImageEntry
import com.lucascamarero.lkvault.security.serialization.ImageMetadata
import com.lucascamarero.lkvault.security.serialization.ImageSerializer
import com.lucascamarero.lkvault.security.crypto.AesGcmCipher
import com.lucascamarero.lkvault.utils.usb.UsbStorageManager
import java.util.UUID

// HU-26: MODELO DE DATOS PARA IMÁGENES CIFRADAS
// HU-27: IMPORTAR IMAGEN Y CIFRARLA
// HU-28: VISUALIZAR IMAGEN DE FORMA SEGURA
// HU-29: ELIMINAR IMAGEN
// Repositorio encargado de gestionar el almacenamiento de imágenes cifradas en el USB.
// Implementa el flujo completo:
// - Lectura de imagen desde URI
// - Cifrado mediante AES-GCM con la Master Key
// - Persistencia en el vault (archivo binario + metadata JSON)
// - Recuperación y descifrado bajo autenticación activa
class ImageRepository(private val context: Context) {

    private val cipher = AesGcmCipher()
    private val storageManager = UsbStorageManager(context)
    private val serializer = ImageSerializer()

    // -------------------------
    // HU-27 - IMPORTAR Y CIFRAR IMAGEN
    // -------------------------
    // Lee una imagen desde un URI, la cifra con la Master Key y la almacena en el USB.
    // Se generan dos archivos:
    // - <id>.img → contenido cifrado
    // - <id>.meta(.json) → metadata (id + nombre)
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

        // Limpieza del buffer de entrada en memoria
        inputBytes.fill(0)

        return true
    }

    // -------------------------
    // HU-28 - LISTAR IMÁGENES
    // -------------------------
    // Recupera todas las imágenes almacenadas en el vault.
    // Lee los archivos de metadata y asocia cada uno con su archivo cifrado correspondiente.
    fun getAllImages(): List<EncryptedImageEntry> {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return emptyList()
        val treeUri = Uri.parse(uriString)

        val imagesDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("images") ?: return emptyList()

        val result = mutableListOf<EncryptedImageEntry>()

        imagesDir.listFiles().forEach { metaFile ->

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
                // Se ignoran entradas corruptas o inconsistentes
            }
        }

        return result
    }

    // -------------------------
    // HU-28 - DESCIFRAR IMAGEN
    // -------------------------
    // Descifra una imagen utilizando la Master Key activa.
    // Devuelve los bytes en claro para su uso temporal en la UI.
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
    // HU-29 - ELIMINAR IMAGEN
    // -------------------------
    // Elimina tanto el archivo cifrado como su metadata asociada.
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

    // -------------------------
    // HU-27 / HU-29 - ACTUALIZAR NOMBRE
    // -------------------------
    // Actualiza el nombre de una imagen modificando únicamente su metadata.
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