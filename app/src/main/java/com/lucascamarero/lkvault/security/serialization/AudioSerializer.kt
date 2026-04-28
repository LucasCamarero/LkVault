package com.lucascamarero.lkvault.security.serialization

import com.google.gson.Gson

// HU-30: GESTIÓN DE ARCHIVOS DE AUDIO
// Representa los metadatos asociados a un audio almacenado en el vault.
// Estos datos se almacenan en formato JSON separado del contenido cifrado,
// permitiendo identificar y mostrar información en la UI sin necesidad de descifrar el audio.
data class AudioMetadata(

    // Identificador único del audio (normalmente nombre de archivo o UUID)
    val id: String,

    // Nombre visible para el usuario (ej: "Tema 1")
    val name: String
)

// HU-30: GESTIÓN DE ARCHIVOS DE AUDIO
// Responsable de la serialización y deserialización de metadatos de audios.
// Convierte entre objetos Kotlin (AudioMetadata) y su representación en JSON.
class AudioSerializer {

    // Librería de Google que sirve para la serialización y deserialización
    private val gson = Gson()

    // Convierte un objeto AudioMetadata a su representación JSON
    fun metadataToJson(meta: AudioMetadata): String {
        return gson.toJson(meta)
    }

    // Convierte una cadena JSON a un objeto AudioMetadata
    fun jsonToMetadata(json: String): AudioMetadata {
        return gson.fromJson(json, AudioMetadata::class.java)
    }
}