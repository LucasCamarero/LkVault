package com.lucascamarero.lkvault.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lucascamarero.lkvault.models.docs.EncryptedDocEntry
import com.lucascamarero.lkvault.security.storage.DocRepository

// HU-32: GESTIÓN DE DOCUMENTOS
// ViewModel encargado de gestionar la interacción entre la UI y el repositorio de documentos.
// Gestiona el flujo completo:
// - Carga de documentos cifrados desde el USB
// - Guardado de nuevos documentos (cifrado)
// - Descifrado bajo sesión autenticada
// - Eliminación y actualización de metadatos
// Expone estados observables para que la UI reaccione a los cambios.
class DocViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DocRepository(application)

    // Lista de documentos cifrados almacenados en el vault
    var docs = mutableStateOf<List<EncryptedDocEntry>>(emptyList())
        private set

    // Documento actualmente seleccionado en memoria (descifrada)
    var selectedDoc = mutableStateOf<ByteArray?>(null)
        private set

    // Carga todos los documentos desde el repositorio
    fun loadDocs() {
        docs.value = repository.getAllDocs()
    }

    // Guarda un nuevo documento en el vault (cifrado con la Master Key)
    fun saveDoc(name: String, uri: Uri, masterKey: ByteArray) {
        val result = repository.saveDoc(name, uri, masterKey)
        if (result) loadDocs()
    }

    // Descifra un documento y lo expone para su uso en la UI
    fun decryptDoc(entry: EncryptedDocEntry, masterKey: ByteArray) {
        selectedDoc.value = repository.decryptDoc(entry, masterKey)
    }

    // Elimina un documento del vault
    fun deleteDoc(id: String) {
        if (repository.deleteDoc(id)) {
            loadDocs()
        }
    }

    // Actualiza el nombre de un documento (modificando su metadata)
    fun updateDocName(id: String, newName: String) {
        if (repository.updateDocName(id, newName)) {
            loadDocs()
        }
    }

    // Limpia el documento seleccionada en memoria
    fun clearSelectedDoc() {
        selectedDoc.value = null
    }
}