package com.lucascamarero.lkvault.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Manager encargado de gestionar el acceso al almacenamiento USB mediante SAF.
 *
 * Centraliza:
 * - almacenamiento persistente de la URI del vault
 * - inicialización del almacenamiento
 * - validación de accesibilidad real del USB
 * - lectura y escritura de archivos dentro del vault
 */
class UsbStorageManager(private val context: Context) {

    private val prefs =
        context.getSharedPreferences("usb_storage", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_URI = "usb_uri"
        private const val VAULT_DIR = "vault"
    }

    /**
     * Guarda la URI seleccionada por el usuario y solicita permisos persistentes
     * de lectura y escritura sobre el árbol SAF.
     *
     * Este permiso permite acceder al almacenamiento incluso tras reiniciar la app.
     *
     * @param uri URI del directorio seleccionado como vault.
     */
    fun saveUri(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        prefs.edit().putString(KEY_URI, uri.toString()).apply()
    }

    /**
     * Recupera la URI persistida del vault.
     *
     * @return URI del vault o null si no existe selección previa.
     */
    fun getUri(): Uri? =
        prefs.getString(KEY_URI, null)?.let { Uri.parse(it) }

    /**
     * Inicializa el vault si aún no existe el archivo sentinela.
     *
     * El archivo sentinela permite identificar el almacenamiento como válido
     * y facilita la detección real de retirada del USB.
     */
    fun initializeIfNeeded() {
        val root = getUri()?.let { DocumentFile.fromTreeUri(context, it) } ?: return

        if (root.findFile("vault.meta") == null) {
            root.createFile("text/plain", "vault.meta")
        }
    }

    /**
     * Comprueba si el almacenamiento USB es accesible realmente.
     *
     * La validación se realiza intentando abrir un stream sobre el archivo sentinela,
     * lo que fuerza acceso físico al volumen y evita falsos positivos del DocumentProvider.
     *
     * @return true si el USB está accesible, false en caso contrario.
     */
    fun isAccessible(): Boolean {
        val root = getUri()?.let { DocumentFile.fromTreeUri(context, it) } ?: return false

        return try {
            val sentinel = root.findFile("vault.meta.txt") ?: return false
            context.contentResolver.openInputStream(sentinel.uri)?.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el directorio raíz del vault como DocumentFile.
     *
     * @return DocumentFile del root del vault o null si la URI no es válida.
     */
    private fun getRoot(): DocumentFile? =
        getUri()?.let { DocumentFile.fromTreeUri(context, it) }

    /**
     * Devuelve el subdirectorio interno donde la aplicación almacena sus archivos.
     * Si no existe, lo crea automáticamente.
     *
     * @return DocumentFile del directorio interno del vault.
     */
    fun getVaultDir(): DocumentFile? {
        val root = getRoot() ?: return null
        return root.findFile(VAULT_DIR) ?: root.createDirectory(VAULT_DIR)
    }

    /**
     * Crea o sobrescribe un archivo dentro del vault.
     *
     * @param name nombre del archivo.
     * @param content contenido en texto plano a guardar.
     */
    fun writeFile(name: String, content: String) {
        val vault = getVaultDir() ?: return

        val file = vault.findFile(name)
            ?: vault.createFile("text/plain", name)

        context.contentResolver.openOutputStream(file!!.uri)?.use {
            it.write(content.toByteArray())
        }
    }

    /**
     * Lee el contenido de un archivo del vault.
     *
     * @param name nombre del archivo a leer.
     * @return contenido del archivo o cadena vacía si no existe.
     */
    fun readFile(name: String): String {
        val vault = getVaultDir() ?: return ""

        val file = vault.findFile(name) ?: return ""

        return context.contentResolver.openInputStream(file.uri)
            ?.bufferedReader()
            ?.readText() ?: ""
    }

    /**
     * Elimina la URI persistida del vault.
     *
     * Puede utilizarse para reinicializar el almacenamiento o cambiar de USB.
     */
    fun clearPermission() {
        prefs.edit().remove(KEY_URI).apply()
    }
}