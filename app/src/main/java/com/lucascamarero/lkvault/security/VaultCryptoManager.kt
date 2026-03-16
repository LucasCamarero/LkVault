package com.lucascamarero.lkvault.security

import android.content.Context

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
// Esta clase centraliza las operaciones criptográficas necesarias
// para inicializar el vault en su primer uso.
//
// Su responsabilidad principal es:
// 1. Generar la Master Key del vault.
// 2. Generar una clave auxiliar.
// 3. Dividir la clave auxiliar mediante secret splitting (2-de-2).
// 4. Almacenar una share en el dispositivo.
// 5. Proteger criptográficamente la clave auxiliar con la clave derivada de la contraseña.
class VaultCryptoManager(private val context: Context) {

    // Generador de la Master Key principal del vault.
    private val masterKeyGenerator = MasterKeyGenerator()

    // Generador de la clave auxiliar utilizada en el esquema de secret splitting.
    private val auxiliaryKeyGenerator = AuxiliaryKeyGenerator()

    // Implementación del algoritmo de división del secreto.
    private val splitter = SecretSplitter()

    // Encargado de almacenar la share correspondiente al dispositivo móvil.
    private val deviceStorage = DeviceShareStorage(context)

    // Clase responsable de proteger (cifrar) la clave auxiliar usando la clave derivada de la contraseña.
    private val protector = MasterKeyProtector()

    // Inicializa la estructura criptográfica del vault.
    //
    // passwordKey: clave derivada de la contraseña del usuario mediante Argon2id.
    //
    // Devuelve un Triple con:
    // 1. masterKey   -> clave maestra del vault
    // 2. encryptedAux -> clave auxiliar cifrada con passwordKey
    // 3. shareUsb    -> parte del secreto que se almacenará en el USB
    fun initializeVault(passwordKey: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {

        // Se genera la Master Key aleatoria que se utilizará para cifrar
        // todos los datos almacenados dentro del vault.
        val masterKey = masterKeyGenerator.generate()

        // Se genera una clave auxiliar independiente.
        val auxiliaryKey = auxiliaryKeyGenerator.generate()

        // La clave auxiliar se divide en dos partes mediante secret splitting.
        val shares = splitter.split(auxiliaryKey)

        // Share destinada a almacenarse en el USB.
        val shareUsb = shares.first

        // Share destinada a almacenarse en el dispositivo móvil.
        val shareDevice = shares.second

        // Se guarda la share del dispositivo en almacenamiento interno.
        deviceStorage.saveShare(shareDevice)

        // La clave auxiliar se protege cifrándola con la clave derivada de la contraseña.
        val encryptedAux = protector.protect(auxiliaryKey, passwordKey)

        // Se devuelve:
        // - masterKey (para uso interno del sistema)
        // - encryptedAux (se almacenará en el USB)
        // - shareUsb (también se almacenará en el USB)
        return Triple(masterKey, encryptedAux, shareUsb)
    }
}