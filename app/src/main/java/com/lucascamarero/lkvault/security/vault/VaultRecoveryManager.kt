package com.lucascamarero.lkvault.security.vault

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.security.securestorage.DeviceShareStorage
import com.lucascamarero.lkvault.security.recovery.RecoveryKeyManager
import com.lucascamarero.lkvault.utils.usb.UsbStorageManager

// HU-14: GENERACIÓN Y GESTIÓN DE RECOVERY KEY
// HU-16: FLUJO DE AUTENTICACIÓN Y RECONSTRUCCIÓN DE MASTER KEY (parcial)
// Esta clase gestiona el proceso de recuperación de acceso al vault utilizando
// una Recovery Key proporcionada por el usuario.
// Su responsabilidad es restaurar la share del dispositivo y reconfigurar
// el acceso al USB, permitiendo que en un flujo posterior se pueda reconstruir la Master Key.
class VaultRecoveryManager(private val context: Context) {

    // Gestor de la Recovery Key (codificación/decodificación Base64)
    private val recoveryManager = RecoveryKeyManager()

    // Almacenamiento seguro de la share del dispositivo (Keystore + prefs)
    private val deviceStorage = DeviceShareStorage(context)

    // Gestor de almacenamiento en USB (utilizado para acceso posterior al vault)
    private val storageManager = UsbStorageManager(context)

    private companion object {
        // Tamaño esperado de la share (32 bytes = 256 bits)
        const val KEY_SIZE = 32
    }

    // Restaura parcialmente el acceso al vault a partir de una Recovery Key y la URI del USB.
    // No reconstruye la Master Key, sino que deja el sistema preparado para que el flujo de
    // autenticación posterior pueda hacerlo.
    fun restoreAccess(recoveryKey: String, treeUri: Uri): Boolean {

        // Se intenta recuperar la share del dispositivo desde la Recovery Key
        val shareDevice = try {
            recoveryManager.recoverDeviceShare(recoveryKey)
        } catch (e: Exception) {
            // Si la clave no es válida (error de decodificación), se aborta
            return false
        }

        // Validación del tamaño de la share
        if (shareDevice.size != KEY_SIZE) {
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

        // Se considera restaurado el acceso
        return true
    }
}