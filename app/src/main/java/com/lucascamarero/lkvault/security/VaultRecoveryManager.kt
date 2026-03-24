package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.utils.UsbStorageManager

class VaultRecoveryManager(private val context: Context) {

    private val splitter = SecretSplitter()
    private val protector = MasterKeyProtector()
    private val recoveryManager = RecoveryKeyManager()
    private val storageManager = UsbStorageManager(context)

    fun recoverVault(recoveryKey: String): ByteArray? {

        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("usb_uri", null) ?: return null
        val treeUri = Uri.parse(uriString)

        val vaultDir = storageManager.getVaultDirectory(treeUri) ?: return null

        // -------- Parse Recovery Key --------
        val recoveryData = try {
            recoveryManager.parseRecoveryKey(recoveryKey)
        } catch (e: Exception) {
            return null
        }

        val shareDevice = recoveryData.shareDevice
        val encryptedAux = recoveryData.encryptedAux

        // -------- Leer share USB --------
        val shareDoc = vaultDir.findFile("masterkey.share") ?: return null
        val shareUsb = context.contentResolver
            .openInputStream(shareDoc.uri)
            ?.readBytes() ?: return null

        // -------- Reconstruir AuxiliaryKey --------
        val auxiliaryKey = splitter.combine(shareUsb, shareDevice)

        // 🔴 limpieza shares (ya no necesarias)
        shareUsb.fill(0)
        shareDevice.fill(0)

        // -------- Leer masterkey.enc --------
        val masterDoc = vaultDir.findFile("masterkey.enc") ?: return null
        val encryptedMasterKey = context.contentResolver
            .openInputStream(masterDoc.uri)
            ?.readBytes() ?: return null

        // -------- Descifrar MasterKey --------
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