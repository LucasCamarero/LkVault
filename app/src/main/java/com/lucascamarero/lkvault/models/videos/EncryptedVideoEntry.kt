package com.lucascamarero.lkvault.models.videos

// HU-31: GESTIÓN DE ARCHIVOS DE VIDEO
// Representa un vídeo almacenado en el vault en su forma cifrada.
// Este modelo corresponde directamente al archivo binario almacenado en el USB.
data class EncryptedVideoEntry(

    // Identificador único (nombre del archivo sin extensión)
    val id: String,

    // Nombre visible para el usuario (ej: "Video 1")
    val name: String,

    // Audio cifrado completo (bytes)
    val encryptedData: ByteArray
)