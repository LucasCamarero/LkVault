package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.models.images.EncryptedImageEntry
import com.lucascamarero.lkvault.security.storage.ImageRepository

// HU-27: IMPORTAR IMAGEN Y CIFRARLA
// HU-28: VISUALIZAR IMAGEN DE FORMA SEGURA
// HU-29: ELIMINAR IMAGEN
// ViewModel encargado de gestionar la interacción entre la UI y el repositorio de imágenes.
// Gestiona el flujo completo:
// - Carga de imágenes cifradas desde el USB
// - Guardado de nuevas imágenes (cifrado)
// - Descifrado bajo sesión autenticada
// - Eliminación y actualización de metadatos
// Expone estados observables para que la UI reaccione a los cambios.
class ImageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ImageRepository(application)

    // Lista de imágenes cifradas almacenadas en el vault
    var images = mutableStateOf<List<EncryptedImageEntry>>(emptyList())
        private set

    // Imagen actualmente seleccionada en memoria (descifrada)
    var selectedImage = mutableStateOf<ByteArray?>(null)
        private set

    // Carga todas las imágenes desde el repositorio
    fun loadImages() {
        images.value = repository.getAllImages()
    }

    // Guarda una nueva imagen en el vault (cifrada con la Master Key)
    fun saveImage(name: String, uri: Uri, masterKey: ByteArray) {
        val result = repository.saveImage(name, uri, masterKey)
        if (result) loadImages()
    }

    // Descifra una imagen y la expone para su uso en la UI
    fun decryptImage(entry: EncryptedImageEntry, masterKey: ByteArray) {
        selectedImage.value = repository.decryptImage(entry, masterKey)
    }

    // Elimina una imagen del vault
    fun deleteImage(id: String) {
        if (repository.deleteImage(id)) {
            loadImages()
        }
    }

    // Actualiza el nombre de una imagen (modificando su metadata)
    fun updateImageName(id: String, newName: String) {
        if (repository.updateImageName(id, newName)) {
            loadImages()
        }
    }

    // Limpia la imagen seleccionada en memoria
    fun clearSelectedImage() {
        selectedImage.value = null
    }
}