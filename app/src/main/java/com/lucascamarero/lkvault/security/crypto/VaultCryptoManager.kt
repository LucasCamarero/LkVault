package com.lucascamarero.lkvault.security.crypto

import android.content.Context
import com.lucascamarero.lkvault.security.key.AuxiliaryKeyGenerator
import com.lucascamarero.lkvault.security.securestorage.DeviceShareStorage
import com.lucascamarero.lkvault.security.key.MasterKeyGenerator
import com.lucascamarero.lkvault.security.keystore.MasterKeyProtector
import com.lucascamarero.lkvault.security.recovery.RecoveryKeyManager
import com.lucascamarero.lkvault.security.recovery.SecretSplitter

// HU-15: FLUJO COMPLETO DE INICIALIZACIÓN CRIPTOGRÁFICA
// Esta clase orquesta todo el proceso de inicialización del vault.
// Se encarga de generar las claves necesarias, aplicar secret splitting,
// proteger las claves mediante cifrado y preparar todos los elementos
// que deben almacenarse en el USB y en el dispositivo.
class VaultCryptoManager(private val context: Context) {

    // Generador de la Master Key (clave principal de cifrado del vault)
    private val masterKeyGenerator = MasterKeyGenerator()

    // Generador de la clave auxiliar utilizada en secret splitting
    private val auxiliaryKeyGenerator = AuxiliaryKeyGenerator()

    // Implementación del esquema de secret splitting (2-de-2)
    private val splitter = SecretSplitter()

    // Almacenamiento seguro de la share del dispositivo
    private val deviceStorage = DeviceShareStorage(context)

    // Protector de claves mediante AES-GCM (envelope encryption)
    private val protector = MasterKeyProtector()

    // Gestor de la Recovery Key
    private val recoveryManager = RecoveryKeyManager()

    // Resultado completo del proceso de inicialización del vault
    data class VaultInitializationResult(
        val encryptedMasterKey: ByteArray,   // Master Key cifrada
        val encryptedAuxiliaryKey: ByteArray, // Auxiliary Key cifrada con la contraseña
        val usbShare: ByteArray,             // Share que se almacenará en el USB
        val recoveryKey: String              // Recovery Key para el usuario
    )

    // Inicializa completamente el vault a partir de la clave derivada de la contraseña
    fun initializeVault(
        passwordKey: ByteArray,
        salt: ByteArray
    ): VaultInitializationResult {

        // -------- 1. Generación de la Master Key --------
        // Clave principal utilizada para cifrar todos los datos del vault
        val masterKey = masterKeyGenerator.generate()

        // -------- 2. Generación de la Auxiliary Key --------
        // Clave intermedia que será dividida mediante secret splitting
        val auxiliaryKey = auxiliaryKeyGenerator.generate()

        // -------- 3. Secret Splitting (2-de-2) --------
        // Se divide la auxiliaryKey en:
        // - shareUsb → almacenada en el USB
        // - shareDevice → almacenada en el dispositivo
        val (shareUsb, shareDevice) = splitter.split(auxiliaryKey)

        // -------- 4. Almacenamiento de la share del dispositivo --------
        // Se guarda cifrada usando Android Keystore
        deviceStorage.saveShare(shareDevice)

        // -------- 5. Protección de la Auxiliary Key con la contraseña --------
        // Se aplica envelope encryption usando la clave derivada de la contraseña
        val encryptedAux = protector.protect(
            auxiliaryKey,
            passwordKey
        )

        // -------- 6. Protección de la Master Key --------
        // La Master Key se cifra utilizando la auxiliaryKey
        val encryptedMasterKey = protector.protect(
            masterKey,
            auxiliaryKey
        )

        // -------- 7. Generación de la Recovery Key --------
        // Se genera una representación en Base64 de la share del dispositivo
        val recoveryKey = recoveryManager.generateRecoveryKey(
            shareDevice
        )

        // -------- Limpieza de memoria --------
        // Se eliminan las claves sensibles de la RAM tras su uso
        auxiliaryKey.fill(0)
        masterKey.fill(0)

        // Se devuelve todo lo necesario para persistir el estado del vault
        return VaultInitializationResult(
            encryptedMasterKey = encryptedMasterKey,
            encryptedAuxiliaryKey = encryptedAux,
            usbShare = shareUsb,
            recoveryKey = recoveryKey
        )
    }
}