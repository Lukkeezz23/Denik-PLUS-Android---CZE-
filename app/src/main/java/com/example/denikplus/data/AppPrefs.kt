// FILE: data/AppPrefs.kt
package com.example.denikplus.data

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

// ✅ Jediná DataStore instance pro celý projekt (SINGLETON přes extension).
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "denikplus_prefs")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    CS("cs"),
    SK("sk"),
    EN("en")
}

class AppPrefs(private val appContext: Context) {

    private val ds = appContext.applicationContext.dataStore

    private object Keys {
        // onboarding / consent
        val CONSENT_GIVEN = booleanPreferencesKey("consent_given")

        // PIN gate (canonical)
        val PIN_PROMPT_DONE = booleanPreferencesKey("pin_prompt_done")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_SALT_B64 = stringPreferencesKey("pin_salt_b64")
        val PIN_HASH_B64 = stringPreferencesKey("pin_hash_b64")

        // appearance
        val THEME_MODE = stringPreferencesKey("theme_mode")   // system/light/dark
        val APP_LANG = stringPreferencesKey("app_language")   // system/cs/sk/en

        // legacy keys (dřívější SettingsDialog) – necháváme kvůli kompatibilitě, ale už nepoužívat:
        val LOCK_ENABLED_LEGACY = booleanPreferencesKey("lock_enabled")
        val LOCK_PIN_HASH_LEGACY = stringPreferencesKey("lock_pin_hash")
    }

    // ---------------------------
    // Consent / onboarding
    // ---------------------------
    val consentGiven: Flow<Boolean> =
        ds.data.map { it[Keys.CONSENT_GIVEN] ?: false }.distinctUntilChanged()

    suspend fun setConsentGiven(value: Boolean) {
        ds.edit { it[Keys.CONSENT_GIVEN] = value }
    }

    val pinPromptDone: Flow<Boolean> =
        ds.data.map { it[Keys.PIN_PROMPT_DONE] ?: false }.distinctUntilChanged()

    suspend fun setPinPromptDone(value: Boolean) {
        ds.edit { it[Keys.PIN_PROMPT_DONE] = value }
    }

    val pinEnabled: Flow<Boolean> =
        ds.data.map { it[Keys.PIN_ENABLED] ?: false }.distinctUntilChanged()

    val hasPinSet: Flow<Boolean> =
        ds.data.map { p ->
            val salt = p[Keys.PIN_SALT_B64].orEmpty()
            val hash = p[Keys.PIN_HASH_B64].orEmpty()
            salt.isNotBlank() && hash.isNotBlank()
        }.distinctUntilChanged()
    suspend fun disablePin() {
        ds.edit {
            it[Keys.PIN_ENABLED] = false
            it.remove(Keys.PIN_SALT_B64)
            it.remove(Keys.PIN_HASH_B64)

            // legacy cleanup (nevadí, když tam nic není)
            it[Keys.LOCK_ENABLED_LEGACY] = false
            it.remove(Keys.LOCK_PIN_HASH_LEGACY)
        }
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        // PIN „enabled“ má smysl jen když je PIN opravdu nastavený (salt+hash existuje).
        val prefs = ds.data.first()
        val ok = prefs[Keys.PIN_SALT_B64].orEmpty().isNotBlank() &&
                prefs[Keys.PIN_HASH_B64].orEmpty().isNotBlank()

        ds.edit {
            it[Keys.PIN_ENABLED] = enabled && ok
            if (!(enabled && ok)) {
            }
        }
    }

    suspend fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val pinBytes = pin.toByteArray(Charsets.UTF_8)
        val hash = sha256(salt + pinBytes)

        ds.edit {
            it[Keys.PIN_ENABLED] = true
            it[Keys.PIN_SALT_B64] = Base64.encodeToString(salt, Base64.NO_WRAP)
            it[Keys.PIN_HASH_B64] = Base64.encodeToString(hash, Base64.NO_WRAP)

            // legacy: nastavíme „lock enabled“, aby starší UI/části nezlobily, pokud někde zůstaly
            it[Keys.LOCK_ENABLED_LEGACY] = true
            it[Keys.LOCK_PIN_HASH_LEGACY] = "" // legacy hash už nepoužíváme
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = ds.data.first()

        val saltB64 = prefs[Keys.PIN_SALT_B64] ?: return false
        val hashB64 = prefs[Keys.PIN_HASH_B64] ?: return false

        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val expected = Base64.decode(hashB64, Base64.NO_WRAP)

        val actual = sha256(salt + pin.toByteArray(Charsets.UTF_8))
        return expected.contentEquals(actual)
    }

    private fun sha256(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(bytes)

    // ---------------------------
    // Theme / Language
    // ---------------------------
    val themeMode: Flow<ThemeMode> =
        ds.data.map { p ->
            when (p[Keys.THEME_MODE] ?: "system") {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }.distinctUntilChanged()

    val appLanguage: Flow<AppLanguage> =
        ds.data.map { p ->
            when (p[Keys.APP_LANG] ?: "system") {
                "cs" -> AppLanguage.CS
                "sk" -> AppLanguage.SK
                "en" -> AppLanguage.EN
                else -> AppLanguage.SYSTEM
            }
        }.distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        ds.edit { p ->
            p[Keys.THEME_MODE] = when (mode) {
                ThemeMode.SYSTEM -> "system"
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
            }
        }
    }

    suspend fun setAppLanguage(lang: AppLanguage) {
        ds.edit { p ->
            p[Keys.APP_LANG] = when (lang) {
                AppLanguage.SYSTEM -> "system"
                AppLanguage.CS -> "cs"
                AppLanguage.SK -> "sk"
                AppLanguage.EN -> "en"
            }
        }
    }

    // ---------------------------------------------------
    // ✅ Kompatibilní API (aby se ti nic jinde nerozbilo)
    // ---------------------------------------------------
    // staré názvy z SettingsDialogu – přesměrování na canonical PIN
    val lockEnabledFlow: Flow<Boolean> = pinEnabled
    val lockPinHashFlow: Flow<String> = ds.data.map { p ->
        // "něco je nastaveno" => vrátíme neprázdný string (SettingsDialog dřív jen testoval blank/not blank)
        val salt = p[Keys.PIN_SALT_B64].orEmpty()
        val hash = p[Keys.PIN_HASH_B64].orEmpty()
        if (salt.isNotBlank() && hash.isNotBlank()) "SET" else ""
    }.distinctUntilChanged()

    suspend fun setLockEnabled(enabled: Boolean) = setPinEnabled(enabled)
    suspend fun setLockPinHash(hash: String) {
        // už se nepoužívá – necháme no-op, aby se nic nerozsypalo
        // (kdyby to někde volalo staré UI)
    }
    suspend fun clearLockPin() = disablePin()
}
