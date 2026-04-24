package com.lucascamarero.lkvault.utils.password

import java.security.SecureRandom

// HU-24: GENERADOR DE CONTRASEÑAS ROBUSTAS
// Esta clase genera contraseñas seguras configurables a partir de distintos tipos de caracteres:
// - Letras (mayúsculas y minúsculas)
// - Números
// - Símbolos
// Utiliza un generador criptográficamente seguro (SecureRandom) y aplica aleatorización
// para evitar patrones predecibles.
class PasswordGenerator {

    private val random = SecureRandom()

    private val lettersLower = "abcdefghijklmnopqrstuvwxyz"
    private val lettersUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val numbers = "0123456789"
    private val symbols = "!@#\$%^&*()-_=+[]{}<>?"

    // Genera una contraseña en función del número de letras, números y símbolos indicados.
    // La longitud final será la suma de todos los parámetros.
    fun generate(
        numLetters: Int,
        numNumbers: Int,
        numSymbols: Int
    ): String {

        val result = mutableListOf<Char>()

        // --- letras ---
        // Se generan letras aleatorias combinando mayúsculas y minúsculas
        repeat(numLetters) {
            val charPool = if (random.nextBoolean())
                lettersLower else lettersUpper

            result.add(charPool[random.nextInt(charPool.length)])
        }

        // Garantiza al menos una mayúscula si se han solicitado letras
        if (numLetters > 0 && result.none { it.isUpperCase() }) {
            result[random.nextInt(result.size)] =
                lettersUpper[random.nextInt(lettersUpper.length)]
        }

        // --- números ---
        // Se añaden números aleatorios
        repeat(numNumbers) {
            result.add(numbers[random.nextInt(numbers.length)])
        }

        // --- símbolos ---
        // Se añaden símbolos especiales aleatorios
        repeat(numSymbols) {
            result.add(symbols[random.nextInt(symbols.length)])
        }

        // --- shuffle ---
        // Se mezcla el resultado para evitar patrones (por ejemplo: letras primero, luego números,
        // etc.)
        result.shuffle(random)

        return result.joinToString("")
    }
}