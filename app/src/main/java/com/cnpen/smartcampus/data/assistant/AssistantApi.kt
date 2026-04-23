package com.cnpen.smartcampus.data.assistant

enum class AssistantApiRole(val wireValue: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant")
}

data class AssistantApiMessage(
    val role: AssistantApiRole,
    val content: String
)

data class AssistantApiRequest(
    val model: String,
    val messages: List<AssistantApiMessage>,
    val temperature: Double = 0.2
)

sealed interface AssistantApiResponse {
    data class Success(val content: String) : AssistantApiResponse
    data class Failure(val message: String) : AssistantApiResponse
}

interface AssistantApi {
    suspend fun createChatCompletion(request: AssistantApiRequest): AssistantApiResponse
}
