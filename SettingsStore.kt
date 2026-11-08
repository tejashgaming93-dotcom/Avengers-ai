package com.avengers.ai.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The Gemini API key lives ONLY in EncryptedSharedPreferences (Android Keystore backed).
 * It is never logged and never shown back in the UI.
 */
class SettingsStore(ctx: Context) {
    private val prefs: SharedPreferences = ctx.getSharedPreferences("tony_settings", Context.MODE_PRIVATE)
    private val secure: SharedPreferences = EncryptedSharedPreferences.create(
        ctx,
        "tony_secure",
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var apiKey: String
        get() = secure.getString("api_key", "") ?: ""
        set(v) { secure.edit().putString("api_key", v.trim()).apply() }

    var provider: String
        get() = prefs.getString("provider", "gemini") ?: "gemini"
        set(v) { prefs.edit().putString("provider", v).apply() }

    var model: String
        get() = prefs.getString("model", MODELS.first()) ?: MODELS.first()
        set(v) { prefs.edit().putString("model", v.trim().ifBlank { MODELS.first() }).apply() }

    /** BCP-47 tag used for speech recognition and text-to-speech. */
    var language: String
        get() = prefs.getString("language", "en-IN") ?: "en-IN"
        set(v) { prefs.edit().putString("language", v).apply() }

    /** Extra user instructions appended to Tony's system prompt. */
    var persona: String
        get() = prefs.getString("persona", "") ?: ""
        set(v) { prefs.edit().putString("persona", v.take(1000)).apply() }

    private val _theme = MutableStateFlow(prefs.getString("theme", "cyan") ?: "cyan")
    val theme: StateFlow<String> = _theme
    fun setTheme(name: String) {
        prefs.edit().putString("theme", name).apply()
        _theme.value = name
    }

    fun hasKey() = apiKey.isNotBlank()

    companion object {
        val MODELS = listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.5-flash-lite")
        val LANGS = listOf(
            "en-IN" to "English / Hinglish",
            "hi-IN" to "हिन्दी",
            "en-US" to "English (US)"
        )
        val THEMES = listOf("cyan" to "Arc Cyan", "red" to "Mark Red", "gold" to "Stark Gold")
    }
}
