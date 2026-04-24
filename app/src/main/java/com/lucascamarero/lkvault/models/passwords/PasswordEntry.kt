package com.lucascamarero.lkvault.models.passwords

// HU-19: MODELO DE DATOS PARA CONTRASEÑAS (FORMATO EN CLARO)
// Esta clase representa una contraseña en su forma descifrada dentro de la aplicación.
// Contiene los datos que el usuario introduce y visualiza en la UI.
// Este modelo SOLO debe existir en memoria durante una sesión autenticada.
data class PasswordEntry(

    // Nombre identificativo del servicio (ej: "BBVA", "Gmail")
    val name: String,

    // Usuario o email asociado a la cuenta
    val username: String,

    // Contraseña en texto plano (solo en memoria)
    val password: String
)
