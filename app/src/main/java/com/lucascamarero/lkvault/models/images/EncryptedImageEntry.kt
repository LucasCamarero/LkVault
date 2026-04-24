package com.lucascamarero.lkvault.models.images

// HU-26: MODELO DE DATOS PARA IMÁGENES CIFRADAS
// Representa una imagen almacenada en el vault en su forma cifrada.
// Este modelo corresponde directamente al archivo binario almacenado en el USB.
data class EncryptedImageEntry(

    // Identificador único (nombre del archivo sin extensión)
    val id: String,

    // Nombre visible para el usuario (ej: "DNI", "Pasaporte")
    val name: String,

    // Imagen cifrada completa (bytes)
    val encryptedData: ByteArray
)