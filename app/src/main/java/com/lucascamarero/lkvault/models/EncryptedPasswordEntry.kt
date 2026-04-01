package com.lucascamarero.lkvault.models

// HU-19: MODELO DE DATOS PARA CONTRASEÑAS CIFRADAS
// Esta clase representa la forma persistida de una contraseña dentro del vault.
// Es el formato que se almacena físicamente en el USB.
// Este modelo NO debe ser utilizado directamente por la UI.
// Debe transformarse primero a PasswordEntry mediante descifrado.
data class EncryptedPasswordEntry(

    // Identificador único de la entrada (UUID o timestamp)
    val id: String,

    // Nombre del servicio (visible para el usuario)
    val name: String,

    // Datos cifrados (username + password serializados y protegidos)
    val encryptedData: ByteArray
)