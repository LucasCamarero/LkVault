package com.lucascamarero.lkvault.utils

import java.security.SecureRandom

// HU24 generador de contraseñas
class PasswordGenerator {

    private val random = SecureRandom()

    private val lettersLower = "abcdefghijklmnopqrstuvwxyz"
    private val lettersUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val numbers = "0123456789"
    private val symbols = "!@#\$%^&*()-_=+[]{}<>?"

    fun generate(
        numLetters: Int,
        numNumbers: Int,
        numSymbols: Int
    ): String {

        val result = mutableListOf<Char>()

        // --- letras ---
        repeat(numLetters) {
            val charPool = if (random.nextBoolean())
                lettersLower else lettersUpper

            result.add(charPool[random.nextInt(charPool.length)])
        }

        // Garantizar al menos una mayúscula
        if (numLetters > 0 && result.none { it.isUpperCase() }) {
            result[random.nextInt(result.size)] =
                lettersUpper[random.nextInt(lettersUpper.length)]
        }

        // --- números ---
        repeat(numNumbers) {
            result.add(numbers[random.nextInt(numbers.length)])
        }

        // --- símbolos ---
        repeat(numSymbols) {
            result.add(symbols[random.nextInt(symbols.length)])
        }

        // --- shuffle ---
        result.shuffle(random)

        return result.joinToString("")
    }
}