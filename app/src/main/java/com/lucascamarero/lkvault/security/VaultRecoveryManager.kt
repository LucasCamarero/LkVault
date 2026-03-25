package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.utils.UsbStorageManager

class VaultRecoveryManager(private val context: Context) {

    private val recoveryManager = RecoveryKeyManager()
    private val deviceStorage = DeviceShareStorage(context)
    private val splitter = SecretSplitter()
    private val protector = MasterKeyProtector()
    private val storageManager = UsbStorageManager(context)

    private companion object {
        const val KEY_SIZE = 32
    }

    fun restoreAccess(recoveryKey: String, treeUri: Uri): Boolean {

        val shareDevice = try {
            recoveryManager.recoverDeviceShare(recoveryKey)
        } catch (e: Exception) {
            return false
        }

        // Validación mínima
        if (shareDevice.size != 32) {
            return false
        }

        // Guardar en Keystore
        deviceStorage.saveShare(shareDevice)
        shareDevice.fill(0)

        // Guardar USB
        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("usb_uri", treeUri.toString())
            .apply()

        return true
    }
}