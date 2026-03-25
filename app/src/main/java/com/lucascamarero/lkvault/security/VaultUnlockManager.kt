package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.utils.UsbStorageManager
import java.io.File

// HU-16: FLUJO DE AUTENTICACIÓN Y RECONSTRUCCIÓN DE MASTER KEY
// Esta clase gestiona el proceso completo de desbloqueo del vault.
// Su responsabilidad es validar la contraseña del usuario, reconstruir la auxiliary key
// mediante secret splitting y finalmente recuperar la Master Key necesaria para descifrar los datos.
class VaultUnlockManager(private val context: Context) {

    // Derivación de clave a partir de la contraseña (Argon2id)
    private val keyDerivation = KeyDerivation()

    // Mecanismo de cifrado/descifrado (AES-GCM)
    private val protector = MasterKeyProtector()

    // Implementación de secret splitting (2-de-2)
    private val splitter = SecretSplitter()

    // Acceso a la share almacenada en el dispositivo
    private val deviceStorage = DeviceShareStorage(context)

    // Gestor de acceso al almacenamiento USB
    private val storageManager = UsbStorageManager(context)

    // Desbloquea el vault a partir de la contraseña del usuario
    fun unlockVault(password: String): ByteArray? {

        // -------- Recuperación de la URI del USB --------
        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return null
        val treeUri = Uri.parse(uriString)

        // Se obtiene el directorio raíz del vault en el USB
        val vaultDir = storageManager.getVaultDirectory(treeUri) ?: return null

        // -------- Lectura de vault.config --------
        val configDoc = vaultDir.findFile("vault.config") ?: return null

        val configBytes = context.contentResolver
            .openInputStream(configDoc.uri)
            ?.readBytes() ?: return null

        // Se utiliza un archivo temporal para parsear la configuración
        val tempConfig = File.createTempFile("vault", ".config", context.cacheDir)
        tempConfig.writeBytes(configBytes)

        val config = VaultConfig.load(tempConfig)

        // Se elimina el archivo temporal tras su uso
        tempConfig.delete()

        // -------- Derivación de clave --------
        val passwordChars = password.toCharArray()

        // Se deriva la clave a partir de la contraseña y el salt almacenado
        val derivedKey = keyDerivation.deriveKey(
            passwordChars,
            config.salt
        )

        // Limpieza de la contraseña en memoria
        passwordChars.fill('\u0000')

        // -------- Recuperación de auxiliary.enc --------
        val auxDoc = vaultDir.findFile("auxiliary.enc") ?: return null

        val encryptedAux = context.contentResolver
            .openInputStream(auxDoc.uri)
            ?.readBytes() ?: return null

        // Se intenta descifrar la auxiliary key usando la clave derivada
        val auxiliaryKey = try {
            protector.recover(encryptedAux, derivedKey)
        } catch (e: Exception) {
            // Si falla (contraseña incorrecta o datos alterados), se aborta
            derivedKey.fill(0)
            return null
        }

        // Limpieza de la clave derivada
        derivedKey.fill(0)

        // -------- Recuperación de share USB --------
        val shareDoc = vaultDir.findFile("masterkey.share") ?: return null

        val shareUsb = context.contentResolver
            .openInputStream(shareDoc.uri)
            ?.readBytes() ?: return null

        // -------- Recuperación de share del dispositivo --------
        val shareDevice = deviceStorage.loadShare() ?: return null

        // -------- Reconstrucción de la auxiliary key --------
        val reconstructed = splitter.combine(shareUsb, shareDevice)

        // -------- Verificación de integridad --------
        // Se comprueba que la reconstrucción coincide con la auxiliary key descifrada
        if (!auxiliaryKey.contentEquals(reconstructed)) {
            auxiliaryKey.fill(0)
            reconstructed.fill(0)
            return null
        }

        // Limpieza del valor reconstruido
        reconstructed.fill(0)

        // -------- Recuperación de masterkey.enc --------
        val masterDoc = vaultDir.findFile("masterkey.enc") ?: return null

        val encryptedMasterKey = context.contentResolver
            .openInputStream(masterDoc.uri)
            ?.readBytes() ?: return null

        // Se descifra la Master Key utilizando la auxiliary key
        val masterKey = try {
            protector.recover(encryptedMasterKey, auxiliaryKey)
        } catch (e: Exception) {
            auxiliaryKey.fill(0)
            return null
        }

        // Limpieza de la auxiliary key
        auxiliaryKey.fill(0)

        // Se devuelve la Master Key lista para su uso en el vault
        return masterKey
    }
}
