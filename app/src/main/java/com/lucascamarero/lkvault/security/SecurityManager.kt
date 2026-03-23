package com.lucascamarero.lkvault.security

import android.content.Context

class SecurityManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val BLOCK_TIME_MS = 60_000L   // bloqueo de 1 minuto

        private const val KEY_ATTEMPTS = "attempts"
        private const val KEY_BLOCK_UNTIL = "block_until"
    }

    fun isBlocked(): Boolean {

        val blockUntil = prefs.getLong(KEY_BLOCK_UNTIL, 0L)
        val now = System.currentTimeMillis()

        return now < blockUntil
    }

    fun getRemainingBlockTime(): Long {

        val blockUntil = prefs.getLong(KEY_BLOCK_UNTIL, 0L)
        val now = System.currentTimeMillis()

        return (blockUntil - now).coerceAtLeast(0L)
    }

    fun registerFailure() {

        val attempts = prefs.getInt(KEY_ATTEMPTS, 0) + 1

        if (attempts >= MAX_ATTEMPTS) {

            val blockUntil = System.currentTimeMillis() + BLOCK_TIME_MS

            prefs.edit()
                .putLong(KEY_BLOCK_UNTIL, blockUntil)
                .putInt(KEY_ATTEMPTS, 0)
                .apply()

        } else {

            prefs.edit()
                .putInt(KEY_ATTEMPTS, attempts)
                .apply()
        }
    }

    fun registerSuccess() {

        prefs.edit()
            .putInt(KEY_ATTEMPTS, 0)
            .putLong(KEY_BLOCK_UNTIL, 0L)
            .apply()
    }

    fun getAttemptsLeft(): Int {

        val attempts = prefs.getInt(KEY_ATTEMPTS, 0)
        return (MAX_ATTEMPTS - attempts).coerceAtLeast(0)
    }
}