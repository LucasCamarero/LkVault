package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.models.EncryptedPasswordEntry
import com.lucascamarero.lkvault.models.PasswordEntry
import com.lucascamarero.lkvault.utils.UsbStorageManager
import java.util.UUID

// HU-20: CREACIÓN Y ALMACENAMIENTO DE CONTRASEÑAS
// Esta clase gestiona la persistencia de contraseñas en el USB.
// Se encarga de:
// - Convertir datos en claro a formato cifrado
// - Aplicar cifrado AES-256-GCM con la Master Key
// - Guardar los datos en el almacenamiento externo (USB)
//
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
}