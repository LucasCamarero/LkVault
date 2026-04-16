package com.lucascamarero.lkvault.security.serialization

// HU-19: PAYLOAD INTERNO PARA CIFRADO DE CONTRASEÑAS
// Esta clase representa la estructura interna que se serializa antes de aplicar cifrado.
// Contiene exclusivamente los datos sensibles de la contraseña.
//
// Flujo:
// PasswordEntry (UI)
//        ↓
// PasswordPayload (serialización)
//        ↓
// AES-GCM (cifrado)
//        ↓
// EncryptedPasswordEntry (persistencia)
data class PasswordPayload(

    // Usuario o email asociado a la cuenta
    val username: String,

    // Contraseña en texto plano (antes de cifrar)
    val password: String
)