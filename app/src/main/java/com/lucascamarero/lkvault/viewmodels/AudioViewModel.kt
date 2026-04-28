package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.models.audios.EncryptedAudioEntry
import com.lucascamarero.lkvault.security.storage.AudioRepository

// HU-30: GESTIÓN DE ARCHIVOS DE AUDIO
// ViewModel encargado de gestionar la interacción entre la UI y el repositorio de audios.
// Gestiona el flujo completo:
// - Carga de audios cifrados desde el USB
// - Guardado de nuevos audios (cifrado)
// - Descifrado bajo sesión autenticada
// - Eliminación y actualización de metadatos
// Expone estados observables para que la UI reaccione a los cambios.
class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioRepository(application)

    // Lista de audios cifrados almacenados en el vault
    var audios = mutableStateOf<List<EncryptedAudioEntry>>(emptyList())
        private set

    // Audio actualmente seleccionado en memoria (descifrada)
    var selectedAudio = mutableStateOf<ByteArray?>(null)
        private set

    // Carga todos los audios desde el repositorio
    fun loadAudios() {
        audios.value = repository.getAllAudios()
    }

    // Guarda un nuevo audio en el vault (cifrado con la Master Key)
    fun saveAudio(name: String, uri: Uri, masterKey: ByteArray) {
        val result = repository.saveAudio(name, uri, masterKey)
        if (result) loadAudios()
    }

    // Descifra un audio y lo expone para su uso en la UI
    fun decryptAudio(entry: EncryptedAudioEntry, masterKey: ByteArray) {
        selectedAudio.value = repository.decryptAudio(entry, masterKey)
    }

    // Elimina un audio del vault
    fun deleteAudio(id: String) {
        if (repository.deleteAudio(id)) {
            loadAudios()
        }
    }

    // Actualiza el nombre de un audio (modificando su metadata)
    fun updateAudioName(id: String, newName: String) {
        if (repository.updateAudioName(id, newName)) {
            loadAudios()
        }
    }

    // Limpia el audio seleccionado en memoria
    fun clearSelectedAudio() {
        selectedAudio.value = null
    }
}