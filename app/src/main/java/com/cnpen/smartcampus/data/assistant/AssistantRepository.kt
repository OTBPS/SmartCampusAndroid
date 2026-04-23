package com.cnpen.smartcampus.data.assistant

enum class AssistantConversationRole {
    USER,
    ASSISTANT
}

data class AssistantConversationMessage(
    val role: AssistantConversationRole,
    val content: String
)

sealed interface AssistantReplyResult {
    data class Success(
        val message: String,
        val contextHint: String
    ) : AssistantReplyResult

    data class Failure(
        val errorMessage: String
    ) : AssistantReplyResult
}

interface AssistantRepository {
    suspend fun ask(
        userMessage: String,
        conversation: List<AssistantConversationMessage>
    ): AssistantReplyResult
}
