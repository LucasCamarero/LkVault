package com.lucascamarero.lkvault.security

import android.content.Context

// HU-12: SECRET SPLITTING
// HU-13: ANDROID KEYSTORE
// HU-14: RECOVERY KEY
// HU-15: INICIALIZACIÓN COMPLETA DEL VAULT

class VaultCryptoManager(private val context: Context) {

    // Generadores y utilidades criptográficas
    private val masterKeyGenerator = MasterKeyGenerator()
    private val auxiliaryKeyGenerator = AuxiliaryKeyGenerator()
    private val splitter = SecretSplitter()
    private val deviceStorage = DeviceShareStorage(context)
    private val protector = MasterKeyProtector()
    private val recoveryManager = RecoveryKeyManager()

    // Resultado de la inicialización
    data class VaultInitializationResult(
        val encryptedMasterKey: ByteArray,
        val encryptedAuxiliaryKey: ByteArray,
        val usbShare: ByteArray,
        val recoveryKey: String
    )

    /**
     * Inicializa completamente el vault:
     *
     * 1. Genera MasterKey
     * 2. Genera AuxiliaryKey
     * 3. Divide la AuxiliaryKey (USB + dispositivo)
     * 4. Guarda share del dispositivo en Keystore
     * 5. Cifra AuxiliaryKey con password (derivedKey)
     * 6. Cifra MasterKey con AuxiliaryKey
     * 7. Genera Recovery Key completa
     */
    fun initializeVault(
        passwordKey: ByteArray,
        salt: ByteArray
    ): VaultInitializationResult {

        // -------- 1. Master Key --------
        val masterKey = masterKeyGenerator.generate()

        // -------- 2. Auxiliary Key --------
        val auxiliaryKey = auxiliaryKeyGenerator.generate()

        // -------- 3. Secret Splitting --------
        val (shareUsb, shareDevice) = splitter.split(auxiliaryKey)

        // -------- 4. Guardar share del dispositivo --------
        deviceStorage.saveShare(shareDevice)

        // -------- 5. Proteger AuxiliaryKey con contraseña --------
        val encryptedAux = protector.protect(
            auxiliaryKey,
            passwordKey
        )

        // -------- 6. Proteger MasterKey (envelope encryption) --------
        val encryptedMasterKey = protector.protect(
            masterKey,
            auxiliaryKey
        )

        // -------- 7. Generar Recovery Key --------
        val recoveryKey = recoveryManager.generateRecoveryKey(
            salt = salt,
            shareDevice = shareDevice,
            encryptedAux = encryptedAux
        )

        // -------- Limpieza básica de memoria (mejora HU18) --------
        auxiliaryKey.fill(0)
        masterKey.fill(0)

        // -------- Resultado --------
        return VaultInitializationResult(
            encryptedMasterKey = encryptedMasterKey,
            encryptedAuxiliaryKey = encryptedAux,
            usbShare = shareUsb,
            recoveryKey = recoveryKey
        )
    }
}