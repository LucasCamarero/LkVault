package com.lucascamarero.lkvault.security.serialization

import com.google.gson.Gson

// HU-31: GESTIÓN DE ARCHIVOS DE VIDEO
// Representa los metadatos asociados a un vídeo almacenado en el vault.
// Estos datos se almacenan en formato JSON separado del contenido cifrado,
// permitiendo identificar y mostrar información en la UI sin necesidad de descifrar el audio.
data class VideoMetadata(

    // Identificador único del vídeo (normalmente nombre de archivo o UUID)
    val id: String,

    // Nombre visible para el usuario (ej: "Video 1")
    val name: String
)

// HU-31: GESTIÓN DE ARCHIVOS DE VIDEO
// Responsable de la serialización y deserialización de metadatos de videos.
// Convierte entre objetos Kotlin (AudioMetadata) y su representación en JSON.
class VideoSerializer {

    // Librería de Google que sirve para la serialización y deserialización
    private val gson = Gson()

    // Convierte un objeto VideoMetadata a su representación JSON
    fun metadataToJson(meta: VideoMetadata): String {
        return gson.toJson(meta)
    }

    // Convierte una cadena JSON a un objeto VideoMetadata
    fun jsonToMetadata(json: String): VideoMetadata {
        return gson.fromJson(json, VideoMetadata::class.java)
    }
}