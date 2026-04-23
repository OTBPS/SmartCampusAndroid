package com.cnpen.smartcampus.ui.assistant

enum class AssistantMessageRole {
    USER,
    ASSISTANT
}

data class AssistantMessage(
    val id: String,
    val role: AssistantMessageRole,
    val content: String
) {
    val isFromUser: Boolean
        get() = role == AssistantMessageRole.USER
}
