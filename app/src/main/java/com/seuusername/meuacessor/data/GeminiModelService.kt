package com.seuusername.meuacessor.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiModelService {
    private const val MODELS_ENDPOINT = "https://generativelanguage.googleapis.com/v1/models"

    suspend fun fetchAvailableModels(apiKey: String): List<String> {
        require(apiKey.isNotBlank()) { "Informe sua API Key para listar os modelos." }

        return withContext(Dispatchers.IO) {
            val url = URL("$MODELS_ENDPOINT?key=$apiKey")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            try {
                val responseCode = connection.responseCode
                val responseStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val body = responseStream.bufferedReader().use { it.readText() }

                if (responseCode !in 200..299) {
                    val errorMessage = JSONObject(body)
                        .optJSONObject("error")
                        ?.optString("message")
                        .orEmpty()

                    throw IllegalStateException(
                        errorMessage.ifBlank { "Falha ao listar modelos (HTTP $responseCode)." }
                    )
                }

                val modelsJson = JSONObject(body).optJSONArray("models") ?: JSONArray()
                buildList {
                    for (index in 0 until modelsJson.length()) {
                        val modelName = modelsJson.optJSONObject(index)?.optString("name").orEmpty()
                        if (modelName.isNotBlank()) add(modelName)
                    }
                }
            } finally {
                connection.disconnect()
            }
        }
    }
}
