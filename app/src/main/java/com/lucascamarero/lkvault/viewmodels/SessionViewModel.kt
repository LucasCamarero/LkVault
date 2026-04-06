package com.lucascamarero.lkvault.viewmodels

import androidx.lifecycle.ViewModel

// HU-16: GESTIÓN DE SESIÓN CRIPTOGRÁFICA (MASTER KEY EN MEMORIA)
// Esta clase gestiona el ciclo de vida de la Master Key dentro de la aplicación.
// Su responsabilidad es mantener la Master Key únicamente en memoria (RAM)
// durante una sesión autenticada, sin persistirla en ningún almacenamiento.
//
// Objetivos de seguridad:
// - Evitar almacenamiento persistente de la Master Key
// - Limitar su exposición únicamente al tiempo de sesión
// - Permitir acceso controlado a las capas que la necesiten (ej: repositorios)
//
// Flujo de uso:
// Login correcto
//      ↓
// VaultUnlockManager reconstruye Master Key
//      ↓
// SessionViewModel almacena la Master Key en memoria
//      ↓
// Pantallas (PasswordScreen, ImageScreen, etc.) acceden a ella
//      ↓
// Logout / cierre / timeout → clear() elimina la clave de memoria
class SessionViewModel : ViewModel() {

    // Referencia interna a la Master Key.
    // Se mantiene privada para evitar modificaciones externas.
    // Puede ser null si no hay sesión activa o tras limpieza.
    private var _masterKey: ByteArray? = null

    // Getter público controlado.
    // Permite acceder a la Master Key sin exponer el setter directamente.
    val masterKey: ByteArray?
        get() = _masterKey

    // Almacena la Master Key en memoria tras autenticación exitosa.
    // Esta clave se utilizará para operaciones criptográficas durante la sesión.
    fun setMasterKey(key: ByteArray) {
        _masterKey = key
    }

    // Elimina la Master Key de memoria de forma segura.
    // - Sobrescribe el contenido del array con ceros (mitigación de memoria residual)
    // - Elimina la referencia para permitir recolección de basura
    //
    // Debe invocarse en:
    // - Logout explícito
    // - Reset del vault
    // - (Opcional) timeout de inactividad
    fun clear() {
        _masterKey?.fill(0)
        _masterKey = null
    }
}