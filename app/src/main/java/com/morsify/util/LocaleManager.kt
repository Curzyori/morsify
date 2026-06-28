package com.morsify.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

/**
 * In-app language switcher. Persists the chosen language in SharedPreferences
 * and wraps the base context with the appropriate locale.
 * Uses recreate() in Activity for locale changes (Compose-only approach).
 */
class LocaleManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var language: String
        get() = prefs.getString(KEY_LANG, DEFAULT) ?: DEFAULT
        set(value) {
            prefs.edit().putString(KEY_LANG, value).apply()
        }

    fun wrap(base: Context): Context {
        val locale = Locale.forLanguageTag(language)
        val cfg = Configuration(base.resources.configuration)
        cfg.setLocale(locale)
        return base.createConfigurationContext(cfg)
    }

    fun applyActivity(act: android.app.Activity) {
        // Locale is applied via wrap() in attachBaseContext.
        // applyActivity is kept for compatibility; locale changes
        // trigger recreate() which re-runs attachBaseContext.
    }

    fun toggle() {
        language = if (language == "id") "en" else "id"
    }

    companion object {
        private const val PREFS = "morsify_prefs"
        private const val KEY_LANG = "lang"
        const val DEFAULT = "id"  // Bahasa Indonesia priority
        val SUPPORTED = listOf("id", "en")
    }
}
