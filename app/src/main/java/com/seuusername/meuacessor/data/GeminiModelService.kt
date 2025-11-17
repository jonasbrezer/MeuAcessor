package com.seuusername.meuacessor.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiModelService {
    private const val BASE_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models"

    suspend fun fetchAvailableModels(apiKey: String): List<String> {
        require(apiKey.isNotBlank()) { "Informe sua API Key para listar os modelos." }

        return withContext(Dispatchers.IO) {
            val url = URL("$BASE_ENDPOINT?key=$apiKey")
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

    suspend fun generateContent(apiKey: String, modelName: String, userMessage: String): String {
        require(apiKey.isNotBlank()) { "Informe sua API Key antes de enviar mensagens." }
        require(userMessage.isNotBlank()) { "A mensagem não pode estar vazia." }

        val sanitizedModelName = modelName.removePrefix("models/").ifBlank {
            throw IllegalArgumentException("Informe um modelo válido nas configurações.")
        }

        return withContext(Dispatchers.IO) {
            val url = URL("$BASE_ENDPOINT/$sanitizedModelName:generateContent?key=$apiKey")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 20_000
                readTimeout = 20_000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }

            val payload = JSONObject().apply {
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().apply {
                            put("role", "user")
                            put(
                                "parts",
                                JSONArray().put(JSONObject().apply { put("text", userMessage) })
                            )
                        }
                    )
                )
            }

            try {
                connection.outputStream.use { output ->
                    output.write(payload.toString().toByteArray())
                }

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
                        errorMessage.ifBlank { "Falha ao gerar resposta (HTTP $responseCode)." }
                    )
                }

                val candidates = JSONObject(body).optJSONArray("candidates") ?: JSONArray()
                for (i in 0 until candidates.length()) {
                    val parts = candidates
                        .optJSONObject(i)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts") ?: continue

                    for (j in 0 until parts.length()) {
                        val text = parts.optJSONObject(j)?.optString("text").orEmpty()
                        if (text.isNotBlank()) {
                            return@withContext text
                        }
                    }
                }

                throw IllegalStateException("A API respondeu sem texto. Tente novamente em instantes.")
            } finally {
                connection.disconnect()
            }
        }
    }
}
