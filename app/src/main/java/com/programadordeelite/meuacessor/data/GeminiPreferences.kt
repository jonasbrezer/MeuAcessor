package com.programadordeelite.meuacessor.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.geminiSettingsDataStore by preferencesDataStore(name = "gemini_settings")

data class GeminiSettings(
    val apiKey: String = "",
    val modelName: String = DEFAULT_GEMINI_MODEL
)

object GeminiPreferences {
    private val API_KEY = stringPreferencesKey("api_key")
    private val MODEL_NAME = stringPreferencesKey("model_name")

    fun observeSettings(context: Context): Flow<GeminiSettings> {
        return context.geminiSettingsDataStore.data.map { prefs ->
            GeminiSettings(
                apiKey = prefs[API_KEY].orEmpty(),
                modelName = prefs[MODEL_NAME].orEmpty().ifBlank { DEFAULT_GEMINI_MODEL }
            )
        }
    }

    suspend fun saveSettings(
        context: Context,
        apiKey: String,
        modelName: String
    ) {
        val sanitizedModel = modelName.removePrefix("models/").ifBlank { DEFAULT_GEMINI_MODEL }
        context.geminiSettingsDataStore.edit { prefs ->
            if (apiKey.isBlank()) {
                prefs.remove(API_KEY)
            } else {
                prefs[API_KEY] = apiKey
            }
            prefs[MODEL_NAME] = sanitizedModel
        }
    }
}
