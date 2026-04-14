package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.models.EncryptedPasswordEntry
import com.lucascamarero.lkvault.models.PasswordEntry
import com.lucascamarero.lkvault.utils.UsbStorageManager
import java.util.UUID

// HU-20: CREACIÓN Y ALMACENAMIENTO DE CONTRASEÑAS
// HU-21: LECTURA DE CONTRASEÑAS DESDE USB
// Esta clase gestiona la persistencia de contraseñas en el USB.
// Se encarga de:
// - Convertir datos en claro a formato cifrado
// - Aplicar cifrado AES-256-GCM con la Master Key
// - Guardar los datos en el almacenamiento externo (USB)
// Recupera todas las contraseñas almacenadas en la carpeta "passwords" del vault.
// Devuelve una lista de EncryptedPasswordEntry (sin descifrar).
// IMPORTANTE:
// - La Master Key debe estar previamente reconstruida (login)
// - Nunca se almacena en esta clase
class PasswordRepository(private val context: Context) {

    private val cipher = AesGcmCipher()
    private val serializer = PasswordSerializer()
    private val storageManager = UsbStorageManager(context)

    // Crea y guarda una contraseña en el USB
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

    fun getAllPasswords(): List<EncryptedPasswordEntry> {

        // -------- 1. Obtener URI del USB --------
        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return emptyList()
        val treeUri = Uri.parse(uriString)

        // -------- 2. Acceder a carpeta passwords --------
        val passwordsDir = storageManager.getPasswordsDirectory(treeUri)
            ?: return emptyList()

        val result = mutableListOf<EncryptedPasswordEntry>()

        // -------- 3. Leer archivos --------
        passwordsDir.listFiles().forEach { file ->

            // Leer contenido JSON
            val json = context.contentResolver
                .openInputStream(file.uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: return@forEach

            try {
                // Convertir JSON → modelo
                val entry = serializer.jsonToEntry(json)

                result.add(entry)

            } catch (e: Exception) {
                // Si el archivo está corrupto o manipulado → se ignora
            }
        }

        // -------- 4. Devolver lista --------
        return result
    }

    // Descifra una entrada utilizando la Master Key
    fun decryptPassword(
        entry: EncryptedPasswordEntry,
        masterKey: ByteArray
    ): PasswordEntry? {

        return try {

            // -------- 1. Descifrar payload --------
            val decryptedBytes = cipher.decrypt(
                entry.encryptedData,
                masterKey
            )

            // -------- 2. Convertir a modelo --------
            val payload = serializer.bytesToPayload(decryptedBytes)

            // -------- 3. Construir objeto final --------
            PasswordEntry(
                name = entry.name,
                username = payload.username,
                password = payload.password
            )

        } catch (e: Exception) {
            // Si falla → clave incorrecta o datos corruptos
            null
        }
    }

    fun decryptPasswordDirect(
        entry: EncryptedPasswordEntry,
        masterKey: ByteArray
    ): PasswordEntry? {
        return decryptPassword(entry, masterKey)
    }

    fun updatePassword(
        entryId: String,
        updatedEntry: PasswordEntry,
        masterKey: ByteArray
    ): Boolean {

        // -------- 1. Obtener URI del USB --------
        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        // -------- 2. Obtener directorio --------
        val passwordsDir = storageManager.getPasswordsDirectory(treeUri)
            ?: return false

        val fileName = "$entryId.pwd"

        val file = passwordsDir.findFile(fileName) ?: return false

        // -------- 3. Crear payload --------
        val payload = PasswordPayload(
            username = updatedEntry.username,
            password = updatedEntry.password
        )

        val payloadBytes = serializer.payloadToBytes(payload)

        // -------- 4. Cifrar --------
        val encryptedData = cipher.encrypt(payloadBytes, masterKey)

        // -------- 5. Reconstruir modelo --------
        val encryptedEntry = EncryptedPasswordEntry(
            id = entryId,
            name = updatedEntry.name,
            encryptedData = encryptedData
        )

        // -------- 6. Serializar --------
        val json = serializer.entryToJson(encryptedEntry)

        // -------- 7. Sobrescribir archivo --------
        storageManager.openOutput(file.uri)?.use {
            it.write(json.toByteArray(Charsets.UTF_8))
        } ?: return false

        // -------- Limpieza --------
        payloadBytes.fill(0)

        return true
    }

    fun deletePassword(entryId: String): Boolean {

        // -------- 1. Obtener URI --------
        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return false
        val treeUri = Uri.parse(uriString)

        // -------- 2. Obtener directorio --------
        val passwordsDir = storageManager.getPasswordsDirectory(treeUri)
            ?: return false

        val fileName = "$entryId.pwd"

        val file = passwordsDir.findFile(fileName) ?: return false

        // -------- 3. Eliminar --------
        return file.delete()
    }
}