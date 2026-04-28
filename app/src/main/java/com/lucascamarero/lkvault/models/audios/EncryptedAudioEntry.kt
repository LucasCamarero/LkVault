package com.lucascamarero.lkvault.models.audios

// HU-30: GESTIÓN DE ARCHIVOS DE AUDIO
// Representa un audio almacenado en el vault en su forma cifrada.
// Este modelo corresponde directamente al archivo binario almacenado en el USB.
data class EncryptedAudioEntry(

    // Identificador único (nombre del archivo sin extensión)
    val id: String,

    // Nombre visible para el usuario (ej: "Canción 1")
    val name: String,

    // Audio cifrado completo (bytes)
    val encryptedData: ByteArray
)