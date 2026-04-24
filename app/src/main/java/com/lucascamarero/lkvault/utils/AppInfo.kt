package com.lucascamarero.lkvault.utils

import android.content.Context

// Devuelve la versión de la aplicación (versionName) definida en build.gradle.
// Si no se puede obtener (error o valor nulo), devuelve una cadena vacía.
fun getAppVersion(context: Context): String {

    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: ""
    } catch (e: Exception) {
        ""
    }

}
