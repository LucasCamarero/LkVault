package com.lucascamarero.lkvault.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.lucascamarero.lkvault.models.language.AppLanguage
import java.util.Locale

// HU-5: SISTEMA DE IDIOMAS
// ViewModel encargado de gestionar el idioma actual de la aplicación
class LanguageViewModel : ViewModel() {

    // Estado observable que almacena el idioma actual de la aplicación.
    var currentLanguage by mutableStateOf(AppLanguage.CASTELLANO)
        private set

    // Inicialización del ViewModel
    init {
        // Obtiene la lista de locales configurados en la aplicación mediante AppCompat
        val currentLocales = AppCompatDelegate.getApplicationLocales()

        // Determina el tag ("es" o "en") del idioma actual
        // Si la aplicación tiene un idioma configurado manualmente se usa ese,
        // en caso contrario se utiliza el idioma del sistema
        val languageTag = if (!currentLocales.isEmpty) {
            currentLocales.toLanguageTags()
        } else {
            Locale.getDefault().language
        }

        // Asigna el idioma interno de la aplicación en función del tag detectado
        currentLanguage = when {
            languageTag.contains("en") -> AppLanguage.INGLES
            else -> AppLanguage.CASTELLANO
        }
    }

    // Cambia el idioma de la aplicación.
    fun changeLanguage(language: AppLanguage) {
        currentLanguage = language

        // Construye la lista de locales a partir del tag del idioma seleccionado
        val localeList = LocaleListCompat.forLanguageTags(language.tag)

        // Aplica el nuevo locale a nivel de aplicación
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}