package com.example.denikplus.data

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

private val Context.dataStore by preferencesDataStore(name = "denikplus_prefs")

class AppPrefs(private val context: Context) {

    private object Keys {
        val CONSENT_GIVEN = booleanPreferencesKey("consent_given")
        val PIN_PROMPT_DONE = booleanPreferencesKey("pin_prompt_done")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_SALT_B64 = stringPreferencesKey("pin_salt_b64")
        val PIN_HASH_B64 = stringPreferencesKey("pin_hash_b64")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val consentGiven: Flow<Boolean> = context.dataStore.data.map { it[Keys.CONSENT_GIVEN] ?: false }
    val pinPromptDone: Flow<Boolean> = context.dataStore.data.map { it[Keys.PIN_PROMPT_DONE] ?: false }
    val pinEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.PIN_ENABLED] ?: false }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    suspend fun setConsentGiven(value: Boolean) {
        context.dataStore.edit { it[Keys.CONSENT_GIVEN] = value }
    }

    suspend fun setPinPromptDone(value: Boolean) {
        context.dataStore.edit { it[Keys.PIN_PROMPT_DONE] = value }
    }

    suspend fun setBiometricEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = value }
    }

    suspend fun disablePin() {
        context.dataStore.edit {
            it[Keys.PIN_ENABLED] = false
            it[Keys.BIOMETRIC_ENABLED] = false
            it.remove(Keys.PIN_SALT_B64)
            it.remove(Keys.PIN_HASH_B64)
        }
    }

    suspend fun setPin(pin: String) {
        val salt: ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val pinBytes: ByteArray = pin.toByteArray(Charsets.UTF_8)
        val hash: ByteArray = sha256(salt + pinBytes)

        context.dataStore.edit {
            it[Keys.PIN_ENABLED] = true
            it[Keys.PIN_SALT_B64] = Base64.encodeToString(salt, Base64.NO_WRAP)
            it[Keys.PIN_HASH_B64] = Base64.encodeToString(hash, Base64.NO_WRAP)
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs: Preferences = context.dataStore.data.first()

        val saltB64: String = prefs[Keys.PIN_SALT_B64] ?: return false
        val hashB64: String = prefs[Keys.PIN_HASH_B64] ?: return false

        val salt: ByteArray = Base64.decode(saltB64, Base64.NO_WRAP)
        val expected: ByteArray = Base64.decode(hashB64, Base64.NO_WRAP)

        val pinBytes: ByteArray = pin.toByteArray(Charsets.UTF_8)
        val actual: ByteArray = sha256(salt + pinBytes)

        return expected.contentEquals(actual)
    }

    private fun sha256(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(bytes)
}
