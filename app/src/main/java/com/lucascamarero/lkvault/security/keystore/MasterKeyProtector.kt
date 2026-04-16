package com.lucascamarero.lkvault.security.keystore

import com.lucascamarero.lkvault.security.crypto.AesGcmCipher

// HU-11: IMPLEMENTACIÓN DE ENVELOPE ENCRYPTION (protección de Master Key):
// Esta clase utiliza una clave derivada de la contraseña (DerivedKey)
// para cifrar y descifrar la Master Key del vault.
class MasterKeyProtector {

    // Se reutiliza el motor de cifrado AES-256-GCM
    private val cipher = AesGcmCipher()

    // Protege la Master Key cifrándola con la clave derivada de la contraseña
    fun protect(masterKey: ByteArray, derivedKey: ByteArray): ByteArray {

        // La Master Key se cifra usando AES-GCM con la derivedKey
        // El resultado contiene: [IV | CIPHERTEXT + TAG]
        return cipher.encrypt(masterKey, derivedKey)
    }

    // Recupera la Master Key descifrando el bloque protegido
    fun recover(encryptedMasterKey: ByteArray, derivedKey: ByteArray): ByteArray {

        // Si la contraseña es incorrecta o los datos han sido modificados
        // AES-GCM lanzará una excepción de autenticación.
        return cipher.decrypt(encryptedMasterKey, derivedKey)
    }
}