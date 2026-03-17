package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.utils.UsbStorageManager
import java.io.File

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
//
// Esta clase se encarga de realizar el proceso necesario para desbloquear el vault.
//
// Flujo de desbloqueo:
//
// 1. Localizar el USB previamente autorizado.
// 2. Leer la configuración criptográfica del vault.
// 3. Derivar la clave desde la contraseña introducida.
// 4. Recuperar la AuxiliaryKey cifrada.
// 5. Recuperar las dos partes del secreto (USB + dispositivo).
// 6. Reconstruir la AuxiliaryKey mediante secret splitting.
// 7. Verificar que la reconstrucción es válida.
// 8. Descifrar la MasterKey mediante envelope encryption.
//
// Si todos los pasos son correctos se devuelve la MasterKey,
// que será utilizada para cifrar y descifrar los datos del vault.
class VaultUnlockManager(private val context: Context) {

    // Encargado de derivar claves desde la contraseña mediante Argon2id.
    private val keyDerivation = KeyDerivation()

    // Clase responsable de cifrar y descifrar claves mediante AES-GCM.
    private val protector = MasterKeyProtector()

    // Implementación del algoritmo de secret splitting 2-de-2.
    private val splitter = SecretSplitter()

    // Encargado de recuperar la share almacenada en el dispositivo móvil.
    private val deviceStorage = DeviceShareStorage(context)

    // Gestor de acceso al almacenamiento USB mediante Storage Access Framework.
    private val storageManager = UsbStorageManager(context)

    // Función principal encargada de desbloquear el vault.
// Recibe la contraseña introducida por el usuario y devuelve la MasterKey
// si el proceso de verificación es correcto.
    fun unlockVault(password: String): ByteArray? {

        // Recuperar URI persistente del USB desde SharedPreferences.
        val prefs = context.getSharedPreferences("usb_prefs", Context.MODE_PRIVATE)

        // Si no existe una URI guardada significa que el usuario no ha concedido
        // acceso al almacenamiento USB previamente.
        val uriString = prefs.getString("usb_uri", null) ?: return null

        // Conversión de la cadena almacenada a un objeto Uri válido.
        val treeUri = Uri.parse(uriString)

        // Obtener el directorio LkVault utilizando el Storage Access Framework.
        val vaultDir = storageManager.getVaultDirectory(treeUri) ?: return null

        // -------- Leer vault.config --------

        val configDoc = vaultDir.findFile("vault.config") ?: return null

        val configBytes = context.contentResolver
            .openInputStream(configDoc.uri)
            ?.readBytes() ?: return null

        val tempConfig = File.createTempFile("vault", ".config", context.cacheDir)

        tempConfig.writeBytes(configBytes)

        val config = VaultConfig.load(tempConfig)

        tempConfig.delete()

        // -------- Derivar clave desde contraseña --------

        val derivedKey = keyDerivation.deriveKey(
            password.toCharArray(),
            config.salt
        )

        // -------- Leer auxiliary.enc --------

        val auxDoc = vaultDir.findFile("auxiliary.enc") ?: return null

        val encryptedAux = context.contentResolver
            .openInputStream(auxDoc.uri)
            ?.readBytes() ?: return null

        // -------- Descifrar AuxiliaryKey --------

        val auxiliaryKey = try {

            protector.recover(
                encryptedAux,
                derivedKey
            )

        } catch (e: Exception) {

            // Contraseña incorrecta o datos corruptos
            return null
        }

        // -------- Leer share USB --------

        val shareDoc = vaultDir.findFile("masterkey.share") ?: return null

        val shareUsb = context.contentResolver
            .openInputStream(shareDoc.uri)
            ?.readBytes() ?: return null

        // -------- Leer share del dispositivo --------

        val shareDevice = deviceStorage.loadShare()
            ?: return null

        // -------- Reconstrucción de AuxiliaryKey --------

        val reconstructed = splitter.combine(
            shareUsb,
            shareDevice
        )

        // -------- Verificación --------

        if (!auxiliaryKey.contentEquals(reconstructed)) {
            return null
        }

        // -------- Leer masterkey.enc --------

        val masterDoc = vaultDir.findFile("masterkey.enc") ?: return null

        val encryptedMasterKey = context.contentResolver
            .openInputStream(masterDoc.uri)
            ?.readBytes() ?: return null

        // -------- Descifrar MasterKey --------

        val masterKey = try {

            protector.recover(
                encryptedMasterKey,
                auxiliaryKey
            )

        } catch (e: Exception) {

            return null
        }

        // -------- Resultado final --------

        return masterKey
    }

}
