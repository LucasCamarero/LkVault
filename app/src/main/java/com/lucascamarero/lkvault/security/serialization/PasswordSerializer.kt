package com.lucascamarero.lkvault.security.serialization

import android.util.Base64
import com.google.gson.Gson
import com.lucascamarero.lkvault.models.passwords.EncryptedPasswordEntry
import kotlin.collections.get

// HU-19: MODELO DE DATOS PARA CONTRASEÑAS CIFRADAS
// Responsable de la serialización y deserialización de contraseñas.
// También gestiona la conversión de datos binarios a Base64 para su almacenamiento en JSON.
class PasswordSerializer {

    // Librería de Google que sirve para la serialización y deserialización
    private val gson = Gson()

    // -------- PASSWORD PAYLOAD --------

    // Convierte un PasswordPayload (datos en claro) a ByteArray mediante JSON.
    // Este formato es el que posteriormente se cifra.
    fun payloadToBytes(payload: PasswordPayload): ByteArray {

        val json = gson.toJson(payload)

        return json.toByteArray(Charsets.UTF_8)
    }

    // Convierte un ByteArray (previamente descifrado) a PasswordPayload.
    fun bytesToPayload(bytes: ByteArray): PasswordPayload {

        val json = String(bytes, Charsets.UTF_8)

        return gson.fromJson(json, PasswordPayload::class.java)
    }

    // -------- ENCRYPTED ENTRY --------

    // Convierte EncryptedPasswordEntry a JSON para su almacenamiento en el USB.
    // El campo encryptedData se codifica en Base64 ya que JSON no soporta datos binarios.
    fun entryToJson(entry: EncryptedPasswordEntry): String {

        val base64Data = Base64.encodeToString(entry.encryptedData, Base64.NO_WRAP)

        val map = mapOf(
            "id" to entry.id,
            "name" to entry.name,
            "encryptedData" to base64Data
        )

        return gson.toJson(map)
    }

    // Convierte un JSON almacenado en el USB a EncryptedPasswordEntry.
    // El campo encryptedData se decodifica desde Base64 a ByteArray.
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