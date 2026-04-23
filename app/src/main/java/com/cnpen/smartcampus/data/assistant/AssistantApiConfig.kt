package com.cnpen.smartcampus.data.assistant

data class AssistantApiConfig(
    val baseUrl: String,
    val model: String,
    val apiKey: String
) {
    val normalizedBaseUrl: String
        get() = baseUrl.trim().trimEnd('/')

    val normalizedModel: String
        get() = model.trim().ifBlank { DEFAULT_MODEL }

    val normalizedApiKey: String
        get() = apiKey.trim()

    companion object {
        const val DEFAULT_MODEL = "deepseek-chat"
        const val DEFAULT_BASE_URL = "https://api.deepseek.com"
    }
}
