package com.cnpen.smartcampus.ui.assistant

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AssistantViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        AssistantUiState(
            messages = listOf(
                AssistantMessage(
                    id = "assistant_1",
                    content = "Hi! I can suggest common campus destinations. Try asking for food, library, or service spots.",
                    isFromUser = false
                ),
                AssistantMessage(
                    id = "assistant_2",
                    content = "For a quick lunch, Cafeteria Center is a popular option near the central walkway.",
                    isFromUser = false
                )
            )
        )
    )
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        _uiState.update { old -> old.copy(inputText = value) }
    }

    fun onSendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val userMessage = AssistantMessage(
            id = "user_${System.currentTimeMillis()}",
            content = text,
            isFromUser = true
        )
        val assistantMessage = AssistantMessage(
            id = "assistant_${System.currentTimeMillis()}",
            content = buildDemoReply(text),
            isFromUser = false
        )

        _uiState.update { old ->
            old.copy(
                inputText = "",
                messages = old.messages + userMessage + assistantMessage
            )
        }
    }

    private fun buildDemoReply(query: String): String {
        val normalized = query.lowercase()
        return when {
            "food" in normalized || "lunch" in normalized || "dinner" in normalized ->
                "You may like Cafeteria Center. It has multiple meal options and is usually active at noon."

            "study" in normalized || "library" in normalized ->
                "Main Library is a strong choice for quiet study and digital resources."

            "service" in normalized || "id" in normalized || "admin" in normalized ->
                "Student Service Hall can help with ID and enrollment support."

            "sports" in normalized || "gym" in normalized ->
                "Sports Center is the best destination for indoor courts and fitness."

            else ->
                "A practical route is to start from Home and use Search filters, then open the selected place on Map."
        }
    }
}
