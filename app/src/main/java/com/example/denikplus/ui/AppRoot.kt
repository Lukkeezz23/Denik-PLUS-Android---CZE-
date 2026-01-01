// FILE: ui/AppRoot.kt
package com.example.denikplus.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import com.example.denikplus.data.AppLanguage
import com.example.denikplus.data.AppPrefs
import com.example.denikplus.data.ThemeMode

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val prefs = remember { AppPrefs(context.applicationContext) }

    val themeMode by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val language by prefs.appLanguage.collectAsState(initial = AppLanguage.SYSTEM)

    // ✅ jazyk aplikace (system/cs/sk/en)
    LaunchedEffect(language) {
        val tags = language.tag
        val locales =
            if (tags == null) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tags)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && dark ->
            dynamicDarkColorScheme(context)

        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !dark ->
            dynamicLightColorScheme(context)

        dark -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        RootGate()
    }
}
