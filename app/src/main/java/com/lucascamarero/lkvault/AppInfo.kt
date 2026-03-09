package com.lucascamarero.lkvault

import android.content.Context

// HU-4: SCREEN MANAGER
// Devuelve la versión de la aplicación (versionName) definida en build.gradle
fun getAppVersion(context: Context): String {

    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: ""
    } catch (e: Exception) {
        ""
    }

}
