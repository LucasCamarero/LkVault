package com.lucascamarero.lkvault.security

import android.content.Context
import android.net.Uri
import com.lucascamarero.lkvault.utils.UsbStorageManager
import java.io.File

// HU-12: IMPLEMENTACIÓN DE SECRET SPLITTING
// Esta clase se encarga de realizar el proceso necesario para desbloquear el vault.
// El proceso incluye:
//
// 1. Localizar el USB previamente autorizado.
// 2. Leer la configuración criptográfica del vault.
// 3. Derivar la clave desde la contraseña introducida.
// 4. Recuperar la clave auxiliar cifrada.
// 5. Recuperar las dos partes del secreto (USB + dispositivo).
// 6. Reconstruir la clave auxiliar mediante secret splitting.
// 7. Verificar que la reconstrucción es válida.
//
// Si todos los pasos son correctos se devuelve la clave auxiliar reconstruida.
class VaultUnlockManager(private val context: Context) {

    // Encargado de derivar claves desde la contraseña mediante Argon2id.
    private val keyDerivation = KeyDerivation()

    // Clase responsable de cifrar y descifrar la clave auxiliar.
    private val protector = MasterKeyProtector()

    // Implementación del algoritmo de secret splitting 2-de-2.
    private val splitter = SecretSplitter()

    // Encargado de recuperar la share almacenada en el dispositivo móvil.
    private val deviceStorage = DeviceShareStorage(context)

    // Gestor de acceso al almacenamiento USB mediante Storage Access Framework.
    private val storageManager = UsbStorageManager(context)

    // Función principal encargada de desbloquear el vault.
    // Recibe la contraseña introducida por el usuario y devuelve la clave auxiliar
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

        // Se localiza el archivo de configuración dentro del vault.
        val configDoc = vaultDir.findFile("vault.config") ?: return null

        // Se leen los bytes del archivo utilizando el ContentResolver.
        val configBytes = context.contentResolver
            .openInputStream(configDoc.uri)
            ?.readBytes() ?: return null

        // Se crea un archivo temporal en la cache de la aplicación
        // para poder reutilizar el method existente de carga.
        val tempConfig = File.createTempFile("vault", ".config", context.cacheDir)

        // Se escriben los bytes del archivo en el archivo temporal.
        tempConfig.writeBytes(configBytes)

        // Se carga la configuración criptográfica del vault.
        val config = VaultConfig.load(tempConfig)

        // Eliminación del archivo temporal.
        tempConfig.delete()

        // -------- Derivar clave desde contraseña --------

        // Se deriva una clave criptográfica a partir de la contraseña introducida
        // utilizando los parámetros almacenados en vault.config.
        val derivedKey = keyDerivation.deriveKey(
            password.toCharArray(),
            config.salt
        )

        // -------- Leer masterkey.enc --------

        // Se localiza el archivo que contiene la clave auxiliar cifrada.
        val encDoc = vaultDir.findFile("masterkey.enc") ?: return null

        // Se leen los bytes del archivo.
        val encryptedAux = context.contentResolver
            .openInputStream(encDoc.uri)
            ?.readBytes() ?: return null

        // -------- Descifrar clave auxiliar --------

        // Se intenta descifrar la clave auxiliar utilizando la clave derivada
        // de la contraseña introducida.
        val auxiliaryKey = try {

            protector.recover(
                encryptedAux,
                derivedKey
            )

        } catch (e: Exception) {

            // Si el descifrado falla (por ejemplo contraseña incorrecta)
            // se devuelve null.
            return null
        }

        // -------- Leer share USB --------

        // Se localiza el archivo que contiene la share almacenada en el USB.
        val shareDoc = vaultDir.findFile("masterkey.share") ?: return null

        // Se leen los bytes de la share del USB.
        val shareUsb = context.contentResolver
            .openInputStream(shareDoc.uri)
            ?.readBytes() ?: return null

        // -------- Leer share del dispositivo --------

        // Se recupera la share almacenada localmente en el dispositivo móvil.
        val shareDevice = deviceStorage.loadShare()
            ?: return null

        // -------- Reconstrucción del secreto --------

        // Se reconstruye la clave auxiliar mediante XOR de ambas shares.
        val reconstructed = splitter.combine(
            shareUsb,
            shareDevice
        )

        // -------- Verificación --------

        // Se comprueba que la clave reconstruida coincide con la clave auxiliar
        // descifrada previamente.
        if (!auxiliaryKey.contentEquals(reconstructed)) {
            return null
        }

        // Si el proceso ha sido correcto se devuelve la clave auxiliar válida.
        return auxiliaryKey
    }
}