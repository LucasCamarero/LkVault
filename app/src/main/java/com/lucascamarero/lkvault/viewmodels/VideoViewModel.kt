package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.models.videos.EncryptedVideoEntry
import com.lucascamarero.lkvault.security.storage.VideoRepository

// HU-31: GESTIÓN DE ARCHIVOS DE VIDEO
// ViewModel encargado de gestionar la interacción entre la UI y el repositorio de vídeos.
// Gestiona el flujo completo:
// - Carga de vídeos cifrados desde el USB
// - Guardado de nuevos vídeos (cifrado)
// - Descifrado bajo sesión autenticada
// - Eliminación y actualización de metadatos
// Expone estados observables para que la UI reaccione a los cambios.
class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)

    // Lista de vídeos cifrados almacenados en el vault
    var videos = mutableStateOf<List<EncryptedVideoEntry>>(emptyList())
        private set

    // Video actualmente seleccionado en memoria (descifrada)
    var selectedVideo = mutableStateOf<ByteArray?>(null)
        private set

    // Carga todos los vídeos desde el repositorio
    fun loadVideos() {
        videos.value = repository.getAllVideos()
    }

    // Guarda un nuevo vídeo en el vault (cifrado con la Master Key)
    fun saveVideo(name: String, uri: Uri, masterKey: ByteArray) {
        val result = repository.saveVideo(name, uri, masterKey)
        if (result) loadVideos()
    }

    // Descifra un vídeo y lo expone para su uso en la UI
    fun decryptVideo(entry: EncryptedVideoEntry, masterKey: ByteArray) {
        selectedVideo.value = repository.decryptVideo(entry, masterKey)
    }

    // Elimina un vídeo del vault
    fun deleteVideo(id: String) {
        if (repository.deleteVideo(id)) {
            loadVideos()
        }
    }

    // Actualiza el nombre de un vídeo (modificando su metadata)
    fun updateVideoName(id: String, newName: String) {
        if (repository.updateVideoName(id, newName)) {
            loadVideos()
        }
    }

    // Limpia el vídeo seleccionado en memoria
    fun clearSelectedVideo() {
        selectedVideo.value = null
    }
}