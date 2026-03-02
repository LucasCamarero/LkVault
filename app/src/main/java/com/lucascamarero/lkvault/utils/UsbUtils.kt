package com.lucascamarero.lkvault.utils

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import java.io.File

// Objeto singleton que contiene utilidades relacionadas
// con la detección y validación de dispositivos externos.
object UsbUtils {

    // Comprueba si existe un dispositivo externo válido.
    // Para considerarse válido debe:
    // 1. Ser un volumen removible (USB o SD).
    // 2. Estar montado (MEDIA_MOUNTED).
    // 3. Contener en su raíz la carpeta "LkVault".
    fun isValidExternalDeviceConnected(context: Context): Boolean {

        // Se obtiene el servicio del sistema encargado de gestionar
        // los volúmenes de almacenamiento del dispositivo.
        val storageManager =
            context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        // Lista de todos los volúmenes disponibles en el sistema
        val volumes = storageManager.storageVolumes

        // Recorremos cada volumen detectado
        for (volume in volumes) {

            // Filtramos únicamente:
            // - Volúmenes físicos removibles (USB o SD)
            // - Que estén actualmente montados y accesibles
            if (volume.isRemovable &&
                volume.state == Environment.MEDIA_MOUNTED
            ) {
                // Obtenemos el directorio raíz del volumen
                val directory = volume.directory

                // Verificamos que el sistema nos permita acceder a él
                if (directory != null) {

                    // Construimos la referencia a la carpeta "LkVault"
                    // que debe existir en la raíz del dispositivo
                    val lkVaultFolder = File(directory, "LkVault")

                    // Comprobamos:
                    // - Que la carpeta exista
                    // - Que realmente sea un directorio
                    if (lkVaultFolder.exists() &&
                        lkVaultFolder.isDirectory
                    ) {
                        // Si se cumplen todas las condiciones,
                        // el dispositivo externo se considera válido
                        return true
                    }
                }
            }
        }

        // Si ningún volumen cumple las condiciones,
        // no hay dispositivo externo válido conectado
        return false
    }
}