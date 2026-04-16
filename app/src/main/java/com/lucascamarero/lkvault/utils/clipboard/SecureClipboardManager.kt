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

//HU25 Portapapeles seguro con autolimpieza
class SecureClipboardManager(
    private val context: Context
) {

    private val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private var job: Job? = null

    // Último texto copiado (para evitar borrar contenido nuevo del usuario)
    private var lastCopiedText: String? = null

    // Copia al portapapeles y lo limpia automáticamente al de 15 segundos
    fun copyWithAutoClear(text: String, timeoutMillis: Long = 15000) {

        // Cancelar limpieza previa
        job?.cancel()

        // Guardar referencia
        lastCopiedText = text

        // Crear clip seguro
        val clip = ClipData.newPlainText("LkVault", text)

        // Marcar como sensible
        clip.description.extras = PersistableBundle().apply {
            putBoolean("android.content.extra.IS_SENSITIVE", true)
        }

        // Copiar al portapapeles
        clipboard.setPrimaryClip(clip)

        // Programar limpieza automática
        job = CoroutineScope(Dispatchers.Main).launch {

            delay(timeoutMillis)

            val current = clipboard.primaryClip

            if (current != null && current.itemCount > 0) {

                val currentText =
                    current.getItemAt(0).coerceToText(context).toString()

                // Solo limpiar si sigue siendo el mismo contenido
                if (currentText == lastCopiedText) {

                    val emptyClip = ClipData.newPlainText("", "")

                    emptyClip.description.extras = PersistableBundle().apply {
                        putBoolean("android.content.extra.IS_SENSITIVE", true)
                    }

                    clipboard.setPrimaryClip(emptyClip)

                    lastCopiedText = null
                }
            }
        }
    }
}