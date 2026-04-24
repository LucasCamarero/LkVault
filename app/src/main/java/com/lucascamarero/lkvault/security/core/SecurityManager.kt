package com.lucascamarero.lkvault.security.core

import android.content.Context

// HU-17: LIMITACIÓN DE INTENTOS DE ACCESO
// Esta clase gestiona el control de intentos de autenticación del usuario.
// Su objetivo es mitigar ataques de fuerza bruta bloqueando temporalmente el acceso
// tras un número determinado de intentos fallidos.
class SecurityManager(context: Context) {

    // SharedPreferences utilizado para persistir el estado de seguridad
    private val prefs =
        context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    companion object {
        // Número máximo de intentos permitidos antes de bloquear
        private const val MAX_ATTEMPTS = 3

        // Tiempo de bloqueo en milisegundos (1 minuto)
        private const val BLOCK_TIME_MS = 60_000L

        // Claves utilizadas en SharedPreferences
        private const val KEY_ATTEMPTS = "attempts"
        private const val KEY_BLOCK_UNTIL = "block_until"
    }

    // Indica si el acceso está actualmente bloqueado
    fun isBlocked(): Boolean {

        // Se obtiene el instante hasta el cual el acceso está bloqueado
        val blockUntil = prefs.getLong(KEY_BLOCK_UNTIL, 0L)

        // Se obtiene el tiempo actual
        val now = System.currentTimeMillis()

        // Si el tiempo actual es menor, el bloqueo sigue activo
        return now < blockUntil
    }

    // Devuelve el tiempo restante de bloqueo en milisegundos
    fun getRemainingBlockTime(): Long {

        // Se obtiene el instante de finalización del bloqueo
        val blockUntil = prefs.getLong(KEY_BLOCK_UNTIL, 0L)

        // Se obtiene el tiempo actual
        val now = System.currentTimeMillis()

        // Se calcula el tiempo restante (nunca negativo)
        return (blockUntil - now).coerceAtLeast(0L)
    }

    // Registra un intento fallido de autenticación
    fun registerFailure() {

        // Se incrementa el contador de intentos fallidos
        val attempts = prefs.getInt(KEY_ATTEMPTS, 0) + 1

        // Si se alcanza el máximo de intentos permitidos
        if (attempts >= MAX_ATTEMPTS) {

            // Se calcula el instante hasta el cual el acceso quedará bloqueado
            val blockUntil = System.currentTimeMillis() + BLOCK_TIME_MS

            // Se guarda el bloqueo y se reinicia el contador de intentos
            prefs.edit()
                .putLong(KEY_BLOCK_UNTIL, blockUntil)
                .putInt(KEY_ATTEMPTS, 0)
                .apply()

        } else {

            // Si no se alcanza el límite, solo se actualiza el contador
            prefs.edit()
                .putInt(KEY_ATTEMPTS, attempts)
                .apply()
        }
    }

    // Registra un inicio de sesión exitoso
    fun registerSuccess() {

        // Se reinician los intentos y se elimina cualquier bloqueo activo
        prefs.edit()
            .putInt(KEY_ATTEMPTS, 0)
            .putLong(KEY_BLOCK_UNTIL, 0L)
            .apply()
    }

    // Devuelve el número de intentos restantes antes del bloqueo
    fun getAttemptsLeft(): Int {

        // Se obtiene el número actual de intentos fallidos
        val attempts = prefs.getInt(KEY_ATTEMPTS, 0)

        // Se calcula cuántos intentos quedan (nunca negativo)
        return (MAX_ATTEMPTS - attempts).coerceAtLeast(0)
    }
}
