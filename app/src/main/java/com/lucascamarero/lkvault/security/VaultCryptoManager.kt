package com.lucascamarero.lkvault.security

import android.content.Context

class VaultCryptoManager(private val context: Context) {

    private val masterKeyGenerator = MasterKeyGenerator()
    private val auxiliaryKeyGenerator = AuxiliaryKeyGenerator()
    private val splitter = SecretSplitter()
    private val deviceStorage = DeviceShareStorage(context)
    private val protector = MasterKeyProtector()
    private val recoveryManager = RecoveryKeyManager()

    data class VaultInitializationResult(
        val encryptedMasterKey: ByteArray,
        val encryptedAuxiliaryKey: ByteArray,
        val usbShare: ByteArray,
        val recoveryKey: String
    )

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

        // -------- 6. Proteger MasterKey --------
        val encryptedMasterKey = protector.protect(
            masterKey,
            auxiliaryKey
        )

        // -------- 7. Generar Recovery Key (NUEVO MODELO) --------
        val recoveryKey = recoveryManager.generateRecoveryKey(
            shareDevice
        )

        // -------- Limpieza --------
        auxiliaryKey.fill(0)
        masterKey.fill(0)

        return VaultInitializationResult(
            encryptedMasterKey = encryptedMasterKey,
            encryptedAuxiliaryKey = encryptedAux,
            usbShare = shareUsb,
            recoveryKey = recoveryKey
        )
    }
}