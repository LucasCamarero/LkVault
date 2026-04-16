package com.lucascamarero.lkvault.security

import com.google.gson.Gson

data class ImageMetadata(
    val id: String,
    val name: String
)

class ImageSerializer {

    private val gson = Gson()

    fun metadataToJson(meta: ImageMetadata): String {
        return gson.toJson(meta)
    }

    fun jsonToMetadata(json: String): ImageMetadata {
        return gson.fromJson(json, ImageMetadata::class.java)
    }
}