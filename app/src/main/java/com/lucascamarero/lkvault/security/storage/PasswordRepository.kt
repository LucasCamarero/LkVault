package com.lucascamarero.lkvault.security.storage

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.models.passwords.EncryptedPasswordEntry
import com.lucascamarero.lkvault.models.passwords.PasswordEntry
import com.lucascamarero.lkvault.security.serialization.PasswordPayload
import com.lucascamarero.lkvault.security.serialization.PasswordSerializer
import com.lucascamarero.lkvault.security.crypto.AesGcmCipher
import com.lucascamarero.lkvault.utils.usb.UsbStorageManager
import java.util.UUID

// HU-20: CREAR CONTRASEÑA
// HU-21: EDITAR CONTRASEÑA
// HU-22: ELIMINAR CONTRASEÑA
// HU-23: VISUALIZAR CONTRASEÑA BAJO AUTENTICACIÓN
// HU-18: LIMPIEZA SEGURA DE CLAVES EN MEMORIA
// Repositorio encargado de gestionar la persistencia de contraseñas en el USB.
// Implementa el flujo completo:
// - Conversión de datos en claro a payload serializable
// - Serialización a JSON
// - Cifrado mediante AES-256-GCM con la Master Key
// - Almacenamiento en archivos individuales (.pwd)
// - Recuperación y descifrado bajo sesión autenticada
// IMPORTANTE:
// - La Master Key debe estar previamente reconstruida (login)
// - Nunca se almacena ni gestiona dentro de esta clase
class PasswordRepository(private val context: Context) {

    private val cipher = AesGcmCipher()
    private val serializer = PasswordSerializer()
    private val storageManager = UsbStorageManager(context)

    // Crea y guarda una nueva contraseña cifrada en el USB
    fun createPassword(
        entry: PasswordEntry,
        masterKey: ByteArray
    ): Boolean {

        // -------- 1. Obtener URI del USB --------
        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        // -------- 2. Convertir a payload --------
        val payload = PasswordPayload(
            username = entry.username,
            password = entry.password
        )

        // -------- 3. Serializar --------
        val payloadBytes = serializer.payloadToBytes(payload)

        // -------- 4. Cifrar --------
        val encryptedData = cipher.encrypt(payloadBytes, masterKey)

        // -------- 5. Crear modelo persistente --------
        val encryptedEntry = EncryptedPasswordEntry(
            id = UUID.randomUUID().toString(),
            name = entry.name,
            encryptedData = encryptedData
        )

        // -------- 6. Convertir a JSON --------
        val json = serializer.entryToJson(encryptedEntry)

        // -------- 7. Guardar en USB --------
        val fileName = "${encryptedEntry.id}.pwd"

        val passwordsDir = storageManager.getPasswordsDirectory(treeUri)
            ?: return false

        val existing = passwordsDir.findFile(fileName)

        val file = existing ?: passwordsDir.createFile(
            "application/octet-stream",
            fileName
        ) ?: return false

        storageManager.openOutput(file.uri)?.use {
            it.write(json.toByteArray(Charsets.UTF_8))
        }

        // -------- Limpieza de memoria --------
        payloadBytes.fill(0)

        return true
    }

    // Recupera todas las contraseñas almacenadas (sin descifrar)
    fun getAllPasswords(): List<EncryptedPasswordEntry> {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return emptyList()
        val treeUri = Uri.parse(uriString)

        val passwordsDir = storageManager.getPasswordsDirectory(treeUri)
            ?: return emptyList()

        val result = mutableListOf<EncryptedPasswordEntry>()

        passwordsDir.listFiles().forEach { file ->

            val json = context.contentResolver
                .openInputStream(file.uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: return@forEach

            try {
                val entry = serializer.jsonToEntry(json)
                result.add(entry)

            } catch (e: Exception) {
                // Se ignoran archivos corruptos o manipulados
            }
        }

        return result
    }

    // Descifra una contraseña utilizando la Master Key activa
    fun decryptPassword(
        entry: EncryptedPasswordEntry,
        masterKey: ByteArray
    ): PasswordEntry? {

        return try {

            val decryptedBytes = cipher.decrypt(
                entry.encryptedData,
                masterKey
            )

            val payload = serializer.bytesToPayload(decryptedBytes)

            PasswordEntry(
                name = entry.name,
                username = payload.username,
                password = payload.password
            )

        } catch (e: Exception) {
            null
        }
    }

    // Alias directo de decryptPassword (facilita uso desde la UI o ViewModel)
    fun decryptPasswordDirect(
        entry: EncryptedPasswordEntry,
        masterKey: ByteArray
    ): PasswordEntry? {
        return decryptPassword(entry, masterKey)
    }

    // Actualiza una contraseña existente sobrescribiendo su archivo
    fun updatePassword(
        entryId: String,
        updatedEntry: PasswordEntry,
        masterKey: ByteArray
    ): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val passwordsDir = storageManager.getPasswordsDirectory(treeUri)
            ?: return false

        val fileName = "$entryId.pwd"

        val file = passwordsDir.findFile(fileName) ?: return false

        val payload = PasswordPayload(
            username = updatedEntry.username,
            password = updatedEntry.password
        )

        val payloadBytes = serializer.payloadToBytes(payload)

        val encryptedData = cipher.encrypt(payloadBytes, masterKey)

        val encryptedEntry = EncryptedPasswordEntry(
            id = entryId,
            name = updatedEntry.name,
            encryptedData = encryptedData
        )

        val json = serializer.entryToJson(encryptedEntry)

        storageManager.openOutput(file.uri)?.use {
            it.write(json.toByteArray(Charsets.UTF_8))
        } ?: return false

        payloadBytes.fill(0)

        return true
    }

    // Elimina una contraseña del vault
    fun deletePassword(entryId: String): Boolean {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        val passwordsDir = storageManager.getPasswordsDirectory(treeUri)
            ?: return false

        val fileName = "$entryId.pwd"

        val file = passwordsDir.findFile(fileName) ?: return false

        return file.delete()
    }
}