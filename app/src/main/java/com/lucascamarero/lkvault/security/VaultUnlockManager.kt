package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.utils.UsbStorageManager
import java.io.File

class VaultUnlockManager(private val context: Context) {

    private val keyDerivation = KeyDerivation()
    private val protector = MasterKeyProtector()
    private val splitter = SecretSplitter()
    private val deviceStorage = DeviceShareStorage(context)
    private val storageManager = UsbStorageManager(context)

    fun unlockVault(password: String): ByteArray? {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return null
        val treeUri = Uri.parse(uriString)

        val vaultDir = storageManager.getVaultDirectory(treeUri) ?: return null

        // -------- vault.config --------
        val configDoc = vaultDir.findFile("vault.config") ?: return null

        val configBytes = context.contentResolver
            .openInputStream(configDoc.uri)
            ?.readBytes() ?: return null

        val tempConfig = File.createTempFile("vault", ".config", context.cacheDir)
        tempConfig.writeBytes(configBytes)

        val config = VaultConfig.load(tempConfig)
        tempConfig.delete()

        // -------- Derivación --------
        val passwordChars = password.toCharArray()

        val derivedKey = keyDerivation.deriveKey(
            passwordChars,
            config.salt
        )

        // 🔴 limpieza password
        passwordChars.fill('\u0000')

        // -------- auxiliary.enc --------
        val auxDoc = vaultDir.findFile("auxiliary.enc") ?: return null

        val encryptedAux = context.contentResolver
            .openInputStream(auxDoc.uri)
            ?.readBytes() ?: return null

        val auxiliaryKey = try {
            protector.recover(encryptedAux, derivedKey)
        } catch (e: Exception) {
            derivedKey.fill(0)
            return null
        }

        // 🔴 limpieza derivedKey
        derivedKey.fill(0)

        // -------- share USB --------
        val shareDoc = vaultDir.findFile("masterkey.share") ?: return null

        val shareUsb = context.contentResolver
            .openInputStream(shareDoc.uri)
            ?.readBytes() ?: return null

        // -------- share dispositivo --------
        val shareDevice = deviceStorage.loadShare() ?: return null

        // -------- reconstrucción --------
        val reconstructed = splitter.combine(shareUsb, shareDevice)

        // -------- verificación --------
        if (!auxiliaryKey.contentEquals(reconstructed)) {
            auxiliaryKey.fill(0)
            reconstructed.fill(0)
            return null
        }

        // 🔴 limpieza reconstructed
        reconstructed.fill(0)

        // -------- masterkey.enc --------
        val masterDoc = vaultDir.findFile("masterkey.enc") ?: return null

        val encryptedMasterKey = context.contentResolver
            .openInputStream(masterDoc.uri)
            ?.readBytes() ?: return null

        val masterKey = try {
            protector.recover(encryptedMasterKey, auxiliaryKey)
        } catch (e: Exception) {
            auxiliaryKey.fill(0)
            return null
        }

        // 🔴 limpieza auxiliaryKey
        auxiliaryKey.fill(0)

        return masterKey
    }
}
