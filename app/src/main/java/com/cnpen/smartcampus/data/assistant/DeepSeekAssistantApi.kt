package com.cnpen.smartcampus.data.assistant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DeepSeekAssistantApi(
    private val config: AssistantApiConfig
) : AssistantApi {
    override suspend fun createChatCompletion(request: AssistantApiRequest): AssistantApiResponse =
        withContext(Dispatchers.IO) {
            val apiKey = config.normalizedApiKey
            if (apiKey.isBlank()) {
                return@withContext AssistantApiResponse.Failure(
                    "DeepSeek API key is missing. Set DEEPSEEK_API_KEY in local.properties."
                )
            }

            val baseUrl = config.normalizedBaseUrl.ifBlank { AssistantApiConfig.DEFAULT_BASE_URL }
            val endpoint = "$baseUrl/chat/completions"
            val payload = JSONObject()
                .put("model", request.model.ifBlank { config.normalizedModel })
                .put("messages", request.messages.toJsonArray())
                .put("temperature", request.temperature)
                .toString()

            runCatching {
                requestChatCompletion(
                    endpoint = endpoint,
                    apiKey = apiKey,
                    payload = payload
                )
            }.fold(
                onSuccess = { responseBody ->
                    parseChatResponse(responseBody)
                },
                onFailure = { throwable ->
                    AssistantApiResponse.Failure(
                        throwable.message ?: "DeepSeek request failed."
                    )
                }
            )
        }

    private fun requestChatCompletion(
        endpoint: String,
        apiKey: String,
        payload: String
    ): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doInput = true
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
        }
        return connection.useAndReturnBody(payload)
    }

    private fun HttpURLConnection.useAndReturnBody(payload: String): String {
        return try {
            outputStream.use { stream ->
                stream.write(payload.toByteArray(Charsets.UTF_8))
            }

            val code = responseCode
            val stream = if (code in 200..299) inputStream else errorStream
            val body = stream?.bufferedText().orEmpty()
            if (code !in 200..299) {
                throw IllegalStateException(parseErrorMessage(body, code))
            }
            if (body.isBlank()) {
                throw IllegalStateException("DeepSeek returned an empty response.")
            }
            body
        } finally {
            disconnect()
        }
    }

    private fun parseChatResponse(responseBody: String): AssistantApiResponse {
        val root = JSONObject(responseBody)
        val choices = root.optJSONArray("choices")
        val firstChoice = choices?.optJSONObject(0)
        val content = firstChoice
            ?.optJSONObject("message")
            ?.optString("content")
            ?.trim()
            .orEmpty()

        return if (content.isNotBlank()) {
            AssistantApiResponse.Success(content)
        } else {
            AssistantApiResponse.Failure("DeepSeek response did not include assistant content.")
        }
    }

    private fun parseErrorMessage(body: String, code: Int): String {
        if (body.isBlank()) {
            return "DeepSeek request failed with HTTP $code."
        }
        val parsed = runCatching {
            JSONObject(body)
                .optJSONObject("error")
                ?.optString("message")
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
        return if (!parsed.isNullOrBlank()) {
            "DeepSeek request failed: $parsed"
        } else {
            "DeepSeek request failed with HTTP $code."
        }
    }

    private fun java.io.InputStream.bufferedText(): String =
        BufferedReader(InputStreamReader(this)).use { it.readText() }

    private fun List<AssistantApiMessage>.toJsonArray(): JSONArray {
        val array = JSONArray()
        forEach { message ->
            array.put(
                JSONObject()
                    .put("role", message.role.wireValue)
                    .put("content", message.content)
            )
        }
        return array
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 20_000
    }
}
