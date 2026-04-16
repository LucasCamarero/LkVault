package com.lucascamarero.lkvault.security.serialization

import android.util.Base64
import com.google.gson.Gson
import com.lucascamarero.lkvault.models.passwords.EncryptedPasswordEntry
import com.lucascamarero.lkvault.security.serialization.PasswordPayload
import kotlin.collections.get

// HU-19: SERIALIZACIÓN DE CONTRASEÑAS
// Esta clase se encarga de convertir objetos de contraseña entre:
// - Objetos Kotlin
// - Representación JSON
// - ByteArray para cifrado
class PasswordSerializer {

    private val gson = Gson()

    // -------- PASSWORD PAYLOAD --------

    // Convierte PasswordPayload a ByteArray (JSON)
    fun payloadToBytes(payload: PasswordPayload): ByteArray {

        val json = gson.toJson(payload)

        return json.toByteArray(Charsets.UTF_8)
    }

    // Convierte ByteArray a PasswordPayload
    fun bytesToPayload(bytes: ByteArray): PasswordPayload {

        val json = String(bytes, Charsets.UTF_8)

        return gson.fromJson(json, PasswordPayload::class.java)
    }

    // -------- ENCRYPTED ENTRY --------

    // Convierte EncryptedPasswordEntry a JSON (para guardar en USB)
    fun entryToJson(entry: EncryptedPasswordEntry): String {

        val base64Data = Base64.encodeToString(entry.encryptedData, Base64.NO_WRAP)

        val map = mapOf(
            "id" to entry.id,
            "name" to entry.name,
            "encryptedData" to base64Data
        )

        return gson.toJson(map)
    }

    // Convierte JSON a EncryptedPasswordEntry
    fun jsonToEntry(json: String): EncryptedPasswordEntry {

        val map = gson.fromJson(json, Map::class.java)

        val id = map["id"] as String
        val name = map["name"] as String
        val encryptedBase64 = map["encryptedData"] as String

        val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)

        return EncryptedPasswordEntry(
            id = id,
            name = name,
            encryptedData = encryptedBytes
        )
    }
}