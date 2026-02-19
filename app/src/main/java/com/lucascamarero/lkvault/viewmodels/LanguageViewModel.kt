package com.lucascamarero.lkvault.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.lucascamarero.lkvault.models.AppLanguage
import java.util.Locale

/*
class LanguageViewModel : ViewModel() {


    var currentLanguage by mutableStateOf(AppLanguage.CASTELLANO)
        private set


    fun changeLanguage(language: AppLanguage) {
        currentLanguage = language

        val localeList = LocaleListCompat.forLanguageTags(language.tag)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}*/

class LanguageViewModel : ViewModel() {

    var currentLanguage by mutableStateOf(AppLanguage.CASTELLANO)
        private set

    init {
        val currentLocales = AppCompatDelegate.getApplicationLocales()

        val languageTag = if (!currentLocales.isEmpty) {
            currentLocales.toLanguageTags()
        } else {
            // Si no hay override, usamos el idioma del sistema
            Locale.getDefault().language
        }

        currentLanguage = when {
            languageTag.contains("en") -> AppLanguage.INGLES
            else -> AppLanguage.CASTELLANO
        }
    }

    fun changeLanguage(language: AppLanguage) {
        currentLanguage = language

        val localeList = LocaleListCompat.forLanguageTags(language.tag)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}