package com.lucascamarero.lkvault.utils.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.PersistableBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// HU-25: PORTAPAPELES SEGURO CON AUTOLIMPIEZA
// Esta clase gestiona el copiado seguro de datos sensibles al portapapeles.
// Permite copiar texto y eliminarlo automáticamente tras un tiempo determinado,
// evitando que información sensible permanezca accesible.
// Incluye una verificación para no sobrescribir contenido nuevo copiado por el usuario.
class SecureClipboardManager(
    private val context: Context
) {

    private val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    // Job utilizado para gestionar la limpieza diferida del portapapeles
    private var job: Job? = null

    // Último texto copiado por esta clase (para evitar borrar contenido nuevo del usuario)
    private var lastCopiedText: String? = null

    // Copia texto al portapapeles y programa su eliminación automática tras un tiempo.
    // Por defecto, el contenido se elimina después de 15 segundos.
    fun copyWithAutoClear(text: String, timeoutMillis: Long = 15000) {

        // Se cancela cualquier limpieza previa pendiente
        job?.cancel()

        // Se guarda referencia del último texto copiado
        lastCopiedText = text

        // Se crea el clip con el contenido sensible
        val clip = ClipData.newPlainText("LkVault", text)

        // Se marca el contenido como sensible (sugerencia al sistema)
        clip.description.extras = PersistableBundle().apply {
            putBoolean("android.content.extra.IS_SENSITIVE", true)
        }

        // Se copia el contenido al portapapeles
        clipboard.setPrimaryClip(clip)

        // Se programa la limpieza automática usando una coroutine
        job = CoroutineScope(Dispatchers.Main).launch {

            delay(timeoutMillis)

            val current = clipboard.primaryClip

            if (current != null && current.itemCount > 0) {

                val currentText =
                    current.getItemAt(0).coerceToText(context).toString()

                // Solo se limpia si el contenido actual coincide con el último copiado
                // evitando borrar contenido nuevo introducido por el usuario
                if (currentText == lastCopiedText) {

                    val emptyClip = ClipData.newPlainText("", "")

                    emptyClip.description.extras = PersistableBundle().apply {
                        putBoolean("android.content.extra.IS_SENSITIVE", true)
                    }

                    // Se sobrescribe el portapapeles con contenido vacío
                    clipboard.setPrimaryClip(emptyClip)

                    lastCopiedText = null
                }
            }
        }
    }
}