package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.models.EncryptedImageEntry
import com.lucascamarero.lkvault.security.ImageRepository

class ImageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ImageRepository(application)

    var images = mutableStateOf<List<EncryptedImageEntry>>(emptyList())
        private set

    var selectedImage = mutableStateOf<ByteArray?>(null)
        private set

    fun loadImages() {
        images.value = repository.getAllImages()
    }

    fun saveImage(name: String, uri: Uri, masterKey: ByteArray) {
        val result = repository.saveImage(name, uri, masterKey)
        if (result) loadImages()
    }

    fun decryptImage(entry: EncryptedImageEntry, masterKey: ByteArray) {
        selectedImage.value = repository.decryptImage(entry, masterKey)
    }

    fun deleteImage(id: String) {
        if (repository.deleteImage(id)) {
            loadImages()
        }
    }

    fun updateImageName(id: String, newName: String) {
        if (repository.updateImageName(id, newName)) {
            loadImages()
        }
    }

    fun clearSelectedImage() {
        selectedImage.value = null
    }
}