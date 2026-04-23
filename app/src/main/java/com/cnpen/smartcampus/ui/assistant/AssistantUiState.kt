package com.cnpen.smartcampus.ui.assistant

data class AssistantUiState(
    val messages: List<AssistantMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val contextHint: String? = null
) {
    val canSend: Boolean
        get() = inputText.trim().isNotEmpty() && !isLoading
}
