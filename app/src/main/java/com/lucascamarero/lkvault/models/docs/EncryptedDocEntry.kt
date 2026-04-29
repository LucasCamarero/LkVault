package com.lucascamarero.lkvault.models.docs

// HU-32: GESTIÓN DE DOCUMENTOS
// Representa un documento almacenado en el vault en su forma cifrada.
// Este modelo corresponde directamente al archivo binario almacenado en el USB.
data class EncryptedDocEntry(

    // Identificador único (nombre del archivo sin extensión)
    val id: String,

    // Nombre visible para el usuario (ej: "CV")
    val name: String,

    // Documento cifrado completo (bytes)
    val encryptedData: ByteArray
)