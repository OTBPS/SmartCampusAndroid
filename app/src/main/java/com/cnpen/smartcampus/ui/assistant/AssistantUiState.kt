package com.cnpen.smartcampus.ui.assistant

data class AssistantUiState(
    val messages: List<AssistantMessage> = emptyList(),
    val inputText: String = ""
)
