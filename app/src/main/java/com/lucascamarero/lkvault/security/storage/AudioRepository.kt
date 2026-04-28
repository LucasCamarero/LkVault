package com.lucascamarero.lkvault.security.storage

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.models.audios.EncryptedAudioEntry
import com.lucascamarero.lkvault.security.serialization.AudioMetadata
import com.lucascamarero.lkvault.security.serialization.AudioSerializer
import com.lucascamarero.lkvault.security.crypto.AesGcmCipher
import com.lucascamarero.lkvault.utils.usb.UsbStorageManager
import java.util.UUID

// HU-30: GESTIÓN DE ARCHIVOS DE AUDIO
// Repositorio encargado de gestionar el almacenamiento de audios cifrados en el USB.
// Implementa el flujo completo:
// - Lectura de audio desde URI
// - Cifrado mediante AES-GCM con la Master Key
// - Persistencia en el vault (archivo binario + metadata JSON)
// - Recuperación y descifrado bajo autenticación activa
class AudioRepository(private val context: Context) {

    private val cipher = AesGcmCipher()
    private val storageManager = UsbStorageManager(context)
    private val serializer = AudioSerializer()

    // Lee un audio desde un URI, lo cifra con la Master Key y lo almacena en el USB.
    // Se generan dos archivos:
    // - <id>.aud → contenido cifrado
    // - <id>.meta(.json) → metadata (id + nombre)
    fun saveAudio(
        name: String,
        audioUri: Uri,
        masterKey: ByteArray
    ): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val audiosDir = storageManager.getAudiosDirectory(treeUri)
            ?: return false

        val id = UUID.randomUUID().toString()

        val inputBytes = context.contentResolver
            .openInputStream(audioUri)
            ?.readBytes() ?: return false

        val encrypted = cipher.encrypt(inputBytes, masterKey)

        // -------- guardar audio cifrada --------
        val audioFile = audiosDir.createFile(
            "application/octet-stream",
            "$id.aud"
        ) ?: return false

        storageManager.openOutput(audioFile.uri)?.use {
            it.write(encrypted)
        }

        // -------- guardar metadata --------
        val meta = AudioMetadata(id, name)

        val metaFile = audiosDir.createFile(
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

    // Recupera todos los audios almacenados en el vault.
    // Lee los archivos de metadata y asocia cada uno con su archivo cifrado correspondiente.
    fun getAllAudios(): List<EncryptedAudioEntry> {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return emptyList()
        val treeUri = Uri.parse(uriString)

        val audiosDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("audios") ?: return emptyList()

        val result = mutableListOf<EncryptedAudioEntry>()

        audiosDir.listFiles().forEach { metaFile ->

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

                // -------- buscar audio cifrado --------
                val audioFile = audiosDir.findFile("${meta.id}.aud")
                    ?: return@forEach

                val encryptedBytes = context.contentResolver
                    .openInputStream(audioFile.uri)
                    ?.readBytes()
                    ?: return@forEach

                // -------- añadir a lista --------
                result.add(
                    EncryptedAudioEntry(
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

    // Descifra un audio utilizando la Master Key activa.
    // Devuelve los bytes en claro para su uso temporal en la UI.
    fun decryptAudio(
        entry: EncryptedAudioEntry,
        masterKey: ByteArray
    ): ByteArray? {

        return try {
            cipher.decrypt(entry.encryptedData, masterKey)
        } catch (e: Exception) {
            null
        }
    }

    // Elimina tanto el archivo cifrado como su metadata asociado.
    fun deleteAudio(id: String): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val audiosDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("audios") ?: return false

        // -------- localizar archivos --------
        val audFile = audiosDir.findFile("$id.aud")
        val metaFile = audiosDir.findFile("$id.meta.json")

        // -------- eliminar --------
        val deletedAud = audFile?.delete() ?: false
        val deletedMeta = metaFile?.delete() ?: false

        return deletedAud && deletedMeta
    }

    // Actualiza el nombre de un audio modificando únicamente su metadata.
    fun updateAudioName(id: String, newName: String): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val audiosDir = storageManager.getVaultDirectory(treeUri)
            ?.findFile("audios") ?: return false

        val metaFile = audiosDir.findFile("$id.meta.json") ?: return false

        return try {
            val meta = AudioMetadata(id, newName)

            storageManager.openOutput(metaFile.uri)?.use {
                it.write(serializer.metadataToJson(meta).toByteArray())
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}