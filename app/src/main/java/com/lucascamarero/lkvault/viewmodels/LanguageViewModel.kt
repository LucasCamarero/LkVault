package com.lucascamarero.lkvault.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.lucascamarero.lkvault.models.AppLanguage

class LanguageViewModel : ViewModel() {


    var currentLanguage by mutableStateOf(AppLanguage.CASTELLANO)
        private set


    fun changeLanguage(language: AppLanguage) {
        currentLanguage = language

        val localeList = LocaleListCompat.forLanguageTags(language.tag)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}