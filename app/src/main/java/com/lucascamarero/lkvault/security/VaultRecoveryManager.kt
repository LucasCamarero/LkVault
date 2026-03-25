package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.utils.UsbStorageManager

// HU-14: GENERACIÓN Y GESTIÓN DE RECOVERY KEY
// HU-16: FLUJO DE AUTENTICACIÓN Y RECONSTRUCCIÓN DE MASTER KEY
// Esta clase gestiona el proceso de recuperación de acceso al vault utilizando
// una Recovery Key proporcionada por el usuario.
// Su responsabilidad principal es restaurar la share del dispositivo y reconfigurar
// el acceso al USB necesario para la reconstrucción futura de la Master Key.
class VaultRecoveryManager(private val context: Context) {

    // Gestor de la Recovery Key (codificación/decodificación Base64)
    private val recoveryManager = RecoveryKeyManager()

    // Almacenamiento seguro de la share del dispositivo (Keystore + prefs)
    private val deviceStorage = DeviceShareStorage(context)

    // Implementación de secret splitting (no usado directamente aquí, pero parte del flujo global)
    private val splitter = SecretSplitter()

    // Protección de la Master Key mediante envelope encryption (no usado directamente aquí)
    private val protector = MasterKeyProtector()

    // Gestor de almacenamiento en USB
    private val storageManager = UsbStorageManager(context)

    private companion object {
        // Tamaño esperado de la share (32 bytes = 256 bits)
        const val KEY_SIZE = 32
    }

    // Restaura el acceso al vault a partir de una Recovery Key y la URI del USB
    fun restoreAccess(recoveryKey: String, treeUri: Uri): Boolean {

        // Se intenta recuperar la share del dispositivo desde la Recovery Key
        val shareDevice = try {
            recoveryManager.recoverDeviceShare(recoveryKey)
        } catch (e: Exception) {
            // Si la clave no es válida (error de decodificación), se aborta
            return false
        }

        // Validación básica del tamaño de la share
        if (shareDevice.size != 32) {
            return false
        }

        // Se guarda la share en el almacenamiento seguro del dispositivo (Keystore)
        deviceStorage.saveShare(shareDevice)

        // Se limpia la share de memoria por seguridad
        shareDevice.fill(0)

        // Se guarda la URI del USB para futuras operaciones
        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("usb_uri", treeUri.toString())
            .apply()

        // Si todo ha ido correctamente, se considera restaurado el acceso
        return true
    }
}