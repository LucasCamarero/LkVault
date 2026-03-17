package com.lucascamarero.lkvault.security

import android.content.Context

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
// HU-13: INTEGRACIÓN CON ANDROID KEYSTORE
// HU-14: GENERACIÓN DE RECOVERY KEY
//
// Esta clase centraliza las operaciones criptográficas necesarias
// para inicializar el vault en su primer uso.
//
// Responsabilidades:
// 1. Generar la Master Key del vault.
// 2. Generar una clave auxiliar.
// 3. Dividir la clave auxiliar mediante secret splitting (2-de-2).
// 4. Almacenar la share del dispositivo protegida con Android Keystore.
// 5. Proteger la clave auxiliar con la clave derivada de la contraseña.
// 6. Proteger la Master Key mediante envelope encryption.
// 7. Generar una Recovery Key para restaurar el acceso si se pierde el dispositivo.
class VaultCryptoManager(private val context: Context) {

    // Generador de la Master Key principal del vault.
    private val masterKeyGenerator = MasterKeyGenerator()

    // Generador de la clave auxiliar utilizada en el esquema de secret splitting.
    private val auxiliaryKeyGenerator = AuxiliaryKeyGenerator()

    // Implementación del algoritmo de división del secreto.
    private val splitter = SecretSplitter()

    // Encargado de almacenar la share correspondiente al dispositivo móvil.
// Internamente utiliza Android Keystore para cifrarla.
    private val deviceStorage = DeviceShareStorage(context)

    // Clase responsable de cifrar y descifrar claves mediante AES-GCM.
    private val protector = MasterKeyProtector()

    // Gestor encargado de generar la Recovery Key.
    private val recoveryManager = RecoveryKeyManager()

    // Resultado de la inicialización criptográfica del vault.
    data class VaultInitializationResult(

        // Master Key cifrada mediante envelope encryption.
        val encryptedMasterKey: ByteArray,

        // Clave auxiliar cifrada con la clave derivada de la contraseña.
        val encryptedAuxiliaryKey: ByteArray,

        // Share del secreto que debe almacenarse en el USB.
        val usbShare: ByteArray,

        // Recovery Key que el usuario debe guardar para recuperar el acceso.
        val recoveryKey: String
    )

    // Inicializa la estructura criptográfica del vault.
//
// passwordKey: clave derivada de la contraseña del usuario mediante Argon2id.
//
// Devuelve un objeto VaultInitializationResult con todos los componentes
// necesarios para finalizar la inicialización del vault.
    fun initializeVault(passwordKey: ByteArray): VaultInitializationResult {

        // -------- Generación de Master Key --------

        val masterKey = masterKeyGenerator.generate()

        // -------- Generación de clave auxiliar --------

        val auxiliaryKey = auxiliaryKeyGenerator.generate()

        // -------- Secret Splitting (2-de-2) --------

        val shares = splitter.split(auxiliaryKey)

        val shareUsb = shares.first
        val shareDevice = shares.second

        // -------- Almacenamiento de la share del dispositivo --------
        // Esta share se almacena cifrada con una clave protegida
        // por Android Keystore.

        deviceStorage.saveShare(shareDevice)

        // -------- Protección de la clave auxiliar --------
        // La auxiliaryKey se cifra con la clave derivada de la contraseña.

        val encryptedAux = protector.protect(auxiliaryKey, passwordKey)

        // -------- Envelope encryption de la Master Key --------
        // La Master Key se cifra utilizando la auxiliaryKey.

        val encryptedMasterKey = protector.protect(masterKey, auxiliaryKey)

        // -------- Generación de Recovery Key --------
        // Permite recuperar la share del dispositivo si se pierde el móvil.

        val recoveryKey = recoveryManager.generateRecoveryKey(shareDevice)

        // -------- Resultado final --------

        return VaultInitializationResult(
            encryptedMasterKey = encryptedMasterKey,
            encryptedAuxiliaryKey = encryptedAux,
            usbShare = shareUsb,
            recoveryKey = recoveryKey
        )
    }
}
