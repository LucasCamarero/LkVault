package com.lucascamarero.lkvault.security.serialization

import com.google.gson.Gson

// HU-32: GESTIÓN DE DOCUMENTOS
// Representa los metadatos asociados a un documento almacenado en el vault.
// Estos datos se almacenan en formato JSON separado del contenido cifrado,
// permitiendo identificar y mostrar información en la UI sin necesidad de descifrar el documento.
data class DocMetadata(

    // Identificador único del documento (normalmente nombre de archivo o UUID)
    val id: String,

    // Nombre visible para el usuario (ej: "CV")
    val name: String
)

// HU-32: GESTIÓN DE DOCUMENTOS
// Responsable de la serialización y deserialización de metadatos de documentos.
// Convierte entre objetos Kotlin (AudioMetadata) y su representación en JSON.
class DocSerializer {

    // Librería de Google que sirve para la serialización y deserialización
    private val gson = Gson()

    // Convierte un objeto DocMetadata a su representación JSON
    fun metadataToJson(meta: DocMetadata): String {
        return gson.toJson(meta)
    }

    // Convierte una cadena JSON a un objeto DocMetadata
    fun jsonToMetadata(json: String): DocMetadata {
        return gson.fromJson(json, DocMetadata::class.java)
    }
}