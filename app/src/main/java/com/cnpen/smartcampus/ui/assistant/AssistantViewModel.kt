package com.cnpen.smartcampus.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.assistant.AssistantConversationMessage
import com.cnpen.smartcampus.data.assistant.AssistantConversationRole
import com.cnpen.smartcampus.data.assistant.AssistantReplyResult
import com.cnpen.smartcampus.data.assistant.AssistantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssistantViewModel(
    private val assistantRepository: AssistantRepository
) : ViewModel() {
    private var lastFailedUserMessage: String? = null

    private val _uiState = MutableStateFlow(
        AssistantUiState(
            messages = listOf(
                AssistantMessage(
                    id = "assistant_welcome",
                    role = AssistantMessageRole.ASSISTANT,
                    content = "Hi! I am your Smart Campus assistant. Ask me about places, routes, or study recommendations."
                )
            )
        )
    )
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        _uiState.update { old -> old.copy(inputText = value) }
    }

    fun onSendMessage() {
        val snapshot = _uiState.value
        val text = snapshot.inputText.trim()
        if (text.isBlank() || snapshot.isLoading) return

        val userMessage = AssistantMessage(
            id = "user_${System.currentTimeMillis()}",
            role = AssistantMessageRole.USER,
            content = text
        )
        val messagesAfterUser = (snapshot.messages + userMessage).takeLast(MAX_UI_MESSAGES)

        _uiState.update { old ->
            old.copy(
                inputText = "",
                messages = messagesAfterUser,
                isLoading = true,
                errorMessage = null
            )
        }

        requestAssistantReply(
            userMessage = text,
            conversation = messagesAfterUser
        )
    }

    fun retryLastRequest() {
        val query = lastFailedUserMessage?.trim().orEmpty()
        if (query.isBlank() || _uiState.value.isLoading) return

        _uiState.update { old ->
            old.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        requestAssistantReply(
            userMessage = query,
            conversation = _uiState.value.messages
        )
    }

    fun dismissError() {
        _uiState.update { old -> old.copy(errorMessage = null) }
    }

    private fun requestAssistantReply(
        userMessage: String,
        conversation: List<AssistantMessage>
    ) {
        viewModelScope.launch {
            when (
                val result = assistantRepository.ask(
                    userMessage = userMessage,
                    conversation = conversation.mapNotNull { it.toConversationMessage() }
                )
            ) {
                is AssistantReplyResult.Success -> {
                    val assistantMessage = AssistantMessage(
                        id = "assistant_${System.currentTimeMillis()}",
                        role = AssistantMessageRole.ASSISTANT,
                        content = result.message
                    )
                    _uiState.update { old ->
                        old.copy(
                            messages = (old.messages + assistantMessage).takeLast(MAX_UI_MESSAGES),
                            isLoading = false,
                            errorMessage = null,
                            contextHint = result.contextHint
                        )
                    }
                    lastFailedUserMessage = null
                }

                is AssistantReplyResult.Failure -> {
                    _uiState.update { old ->
                        old.copy(
                            isLoading = false,
                            errorMessage = result.errorMessage
                        )
                    }
                    lastFailedUserMessage = userMessage
                }
            }
        }
    }

    private fun AssistantMessage.toConversationMessage(): AssistantConversationMessage? {
        val normalized = content.trim()
        if (normalized.isBlank()) return null
        val mappedRole = when (role) {
            AssistantMessageRole.USER -> AssistantConversationRole.USER
            AssistantMessageRole.ASSISTANT -> AssistantConversationRole.ASSISTANT
        }
        return AssistantConversationMessage(
            role = mappedRole,
            content = normalized
        )
    }

    private companion object {
        const val MAX_UI_MESSAGES = 30
    }
}
