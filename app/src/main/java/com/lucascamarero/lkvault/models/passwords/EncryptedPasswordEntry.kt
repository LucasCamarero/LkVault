package com.lucascamarero.lkvault.models.passwords

// HU-19: MODELO DE DATOS PARA CONTRASEÑAS CIFRADAS
// Esta clase representa la forma persistida de una contraseña dentro del vault.
// Es el formato que se almacena físicamente en el USB.
data class EncryptedPasswordEntry(

    // Identificador único de la entrada (UUID o timestamp)
    val id: String,

    // Nombre del servicio (visible para el usuario)
    val name: String,

    // Datos cifrados (username + password serializados y protegidos)
    val encryptedData: ByteArray
)