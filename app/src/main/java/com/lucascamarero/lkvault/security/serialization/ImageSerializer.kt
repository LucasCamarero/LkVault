package com.lucascamarero.lkvault.security.serialization

import com.google.gson.Gson

// HU-26: MODELO DE DATOS PARA IMÁGENES CIFRADAS
// Representa los metadatos asociados a una imagen almacenada en el vault.
// Estos datos se almacenan en formato JSON separado del contenido cifrado,
// permitiendo identificar y mostrar información en la UI sin necesidad de descifrar la imagen.
data class ImageMetadata(

    // Identificador único de la imagen (normalmente nombre de archivo o UUID)
    val id: String,

    // Nombre visible para el usuario (ej: "DNI", "Pasaporte")
    val name: String
)

// HU-26: MODELO DE DATOS PARA IMÁGENES CIFRADAS
// Responsable de la serialización y deserialización de metadatos de imágenes.
// Convierte entre objetos Kotlin (ImageMetadata) y su representación en JSON.
class ImageSerializer {

    // Librería de Google que sirve para la serialización y deserialización
    private val gson = Gson()

    // Convierte un objeto ImageMetadata a su representación JSON
    fun metadataToJson(meta: ImageMetadata): String {
        return gson.toJson(meta)
    }

    // Convierte una cadena JSON a un objeto ImageMetadata
    fun jsonToMetadata(json: String): ImageMetadata {
        return gson.fromJson(json, ImageMetadata::class.java)
    }
}