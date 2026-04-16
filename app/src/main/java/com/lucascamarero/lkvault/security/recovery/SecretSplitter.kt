package com.lucascamarero.lkvault.security.recovery

import java.security.SecureRandom

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
// Esta clase implementa un esquema simple de secret splitting 2-de-2.
// El secreto original se divide en dos partes (shares) que son necesarias
// conjuntamente para reconstruir el valor original.
// Si solo se posee una de las dos partes, no es posible recuperar el secreto.
class SecretSplitter {

    // Generador criptográficamente seguro utilizado para crear una de las shares aleatorias.
    private val secureRandom = SecureRandom()

    // Divide un secreto en dos partes independientes.
    // La primera share se genera aleatoriamente.
    // La segunda share se calcula mediante XOR entre el secreto original y la primera share.
    fun split(secret: ByteArray): Pair<ByteArray, ByteArray> {

        // Share que se almacenará en el USB.
        // Se genera como una secuencia de bytes aleatorios del mismo tamaño que el secreto.
        val shareUsb = ByteArray(secret.size)
        secureRandom.nextBytes(shareUsb)

        // Share que se almacenará en el dispositivo móvil.
        val shareDevice = ByteArray(secret.size)

        // Se calcula cada byte de la segunda share mediante XOR.
        // secret = shareUsb XOR shareDevice
        // Por tanto: shareDevice = secret XOR shareUsb
        for (i in secret.indices) {
            shareDevice[i] = (secret[i].toInt() xor shareUsb[i].toInt()).toByte()
        }

        // Se devuelven ambas shares.
        // shareUsb → se almacenará en el USB
        // shareDevice → se almacenará en el dispositivo móvil
        return Pair(shareUsb, shareDevice)
    }

    // Reconstruye el secreto original a partir de las dos shares.
    // Utiliza nuevamente la propiedad del XOR:
    // secret = shareUsb XOR shareDevice
    fun combine(shareUsb: ByteArray, shareDevice: ByteArray): ByteArray {

        // Array que contendrá el secreto reconstruido.
        val result = ByteArray(shareUsb.size)

        // Se aplica XOR byte a byte entre ambas shares.
        for (i in shareUsb.indices) {
            result[i] = (shareUsb[i].toInt() xor shareDevice[i].toInt()).toByte()
        }

        // Se devuelve el secreto reconstruido.
        return result
    }
}