package com.cnpen.smartcampus.data.assistant

class DeepSeekAssistantRepository(
    private val modelName: String,
    private val assistantApi: AssistantApi,
    private val contextBuilder: AssistantContextBuilder
) : AssistantRepository {

    override suspend fun ask(
        userMessage: String,
        conversation: List<AssistantConversationMessage>
    ): AssistantReplyResult {
        val query = userMessage.trim()
        if (query.isBlank()) {
            return AssistantReplyResult.Failure("Please enter a message.")
        }

        val context = contextBuilder.build(query)
        val history = conversation
            .takeLast(MAX_HISTORY_MESSAGES)
            .mapNotNull { it.toApiMessage() }
        val groundedConversation = history.withGroundedLastUserPrompt(
            userMessage = query,
            contextJson = context.toJsonString()
        )

        val response = assistantApi.createChatCompletion(
            request = AssistantApiRequest(
                model = modelName,
                messages = listOf(
                    AssistantApiMessage(
                        role = AssistantApiRole.SYSTEM,
                        content = SYSTEM_PROMPT
                    )
                ) + groundedConversation
            )
        )

        return when (response) {
            is AssistantApiResponse.Success -> {
                AssistantReplyResult.Success(
                    message = response.content,
                    contextHint = context.buildContextHint()
                )
            }

            is AssistantApiResponse.Failure -> {
                AssistantReplyResult.Failure(
                    errorMessage = response.message
                )
            }
        }
    }

    private fun AssistantConversationMessage.toApiMessage(): AssistantApiMessage? {
        val text = content.trim()
        if (text.isBlank()) return null
        val apiRole = when (role) {
            AssistantConversationRole.USER -> AssistantApiRole.USER
            AssistantConversationRole.ASSISTANT -> AssistantApiRole.ASSISTANT
        }
        return AssistantApiMessage(
            role = apiRole,
            content = text
        )
    }

    private fun List<AssistantApiMessage>.withGroundedLastUserPrompt(
        userMessage: String,
        contextJson: String
    ): List<AssistantApiMessage> {
        if (isEmpty()) {
            return listOf(
                AssistantApiMessage(
                    role = AssistantApiRole.USER,
                    content = buildGroundedUserPrompt(userMessage, contextJson)
                )
            )
        }
        val last = last()
        if (last.role == AssistantApiRole.USER) {
            return dropLast(1) + last.copy(
                content = buildGroundedUserPrompt(userMessage, contextJson)
            )
        }
        return this + AssistantApiMessage(
            role = AssistantApiRole.USER,
            content = buildGroundedUserPrompt(userMessage, contextJson)
        )
    }

    private fun buildGroundedUserPrompt(
        userMessage: String,
        contextJson: String
    ): String {
        return """
User request:
$userMessage

Campus context (JSON):
$contextJson

Please answer based on this context first. If data is insufficient, say that clearly.
""".trimIndent()
    }

    private companion object {
        const val MAX_HISTORY_MESSAGES = 8

        val SYSTEM_PROMPT = """
You are Smart Campus Assistant for NUIST.
Use the provided structured campus context as your primary source.
Do not fabricate places or route facts that are not in context.
Focus on practical campus support:
1) POI explanation
2) Route explanation
3) Campus recommendation
Keep responses concise, clear, and useful.
If context is insufficient, state the limitation explicitly.
For recommendations, suggest 1 to 3 places with short reasons.
""".trimIndent()
    }
}
