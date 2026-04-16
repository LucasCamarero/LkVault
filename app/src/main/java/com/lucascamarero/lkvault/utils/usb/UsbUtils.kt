package com.lucascamarero.lkvault.utils.usb

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import java.io.File

// HU-6: DETECCIÓN Y VALIDACIÓN DE USB CONECTADO
// Este objeto singleton proporciona utilidades para detectar y validar
// la presencia de un dispositivo de almacenamiento externo (USB o SD).
// Aplica criterios estrictos para garantizar que el dispositivo es válido
// para operar con el vault (montado, removible y con estructura correcta).
object UsbUtils {

    // Nombre de la carpeta raíz del vault que debe existir en el USB
    private const val VAULT_FOLDER_NAME = "LkVault"

    // Comprueba si existe un dispositivo externo válido conectado
    fun isValidExternalDeviceConnected(context: Context): Boolean {

        // Se intenta obtener un volumen válido
        val root = getValidExternalRoot(context)

        // Si existe, el dispositivo es válido
        return root != null
    }

    // Devuelve el directorio raíz del dispositivo externo válido o null si no existe
    fun getValidExternalRoot(context: Context): File? {

        // Se obtiene el servicio del sistema encargado de gestionar los volúmenes de almacenamiento
        val storageManager =
            context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        // Se obtiene la lista de todos los volúmenes disponibles
        val volumes = storageManager.storageVolumes

        // Se recorren todos los volúmenes detectados
        for (volume in volumes) {

            // Se filtran únicamente los volúmenes:
            // - removibles (USB o SD)
            // - que estén montados y accesibles
            if (volume.isRemovable &&
                volume.state == Environment.MEDIA_MOUNTED
            ) {

                // Se obtiene el directorio raíz del volumen
                val directory = volume.directory

                if (directory != null) {

                    // Se construye la ruta a la carpeta "LkVault"
                    val lkVaultFolder = File(directory, VAULT_FOLDER_NAME)

                    // Se comprueba que:
                    // - la carpeta exista
                    // - sea realmente un directorio
                    if (lkVaultFolder.exists() &&
                        lkVaultFolder.isDirectory
                    ) {
                        // Si cumple las condiciones, se devuelve este volumen como válido
                        return directory
                    }
                }
            }
        }

        // Si ningún volumen cumple los requisitos, se devuelve null
        return null
    }
}