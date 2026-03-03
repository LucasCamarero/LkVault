package com.lucascamarero.lkvault.models

// Enum que representa los idiomas disponibles en la aplicación.
//
// Cada valor contiene:
// - tag → Código ISO del idioma (language tag)
//          utilizado por AppCompatDelegate para aplicar el locale.
enum class AppLanguage(val tag: String) {

    // Español (código ISO: "es")
    CASTELLANO("es"),

    // Inglés (código ISO: "en")
    INGLES("en")
}