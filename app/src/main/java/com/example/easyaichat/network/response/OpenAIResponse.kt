package com.example.easyaichat.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class OpenAIResponse(
    val id: String,
    @SerialName("object")
    val obj: String, // Corrected field name mapping
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage?
) {
    @Serializable
    data class Choice(
        val index: Int,
        val message: Message,
        @SerialName("finish_reason")
        val finishReason: String,
        val logprobs: String? = null // Added nullable `logprobs` field for completeness
    )

    @Serializable
    data class Message(
        val role: String,
        val content: String,
        val refusal: String? = null // Added nullable `refusal` field for completeness
    )

    @Serializable
    data class Usage(
        @SerialName("prompt_tokens")
        val promptTokens: Int,
        @SerialName("completion_tokens")
        val completionTokens: Int,
        @SerialName("total_tokens")
        val totalTokens: Int,
        @SerialName("prompt_tokens_details")
        val promptTokensDetails: PromptTokensDetails? = null, // Made optional
        @SerialName("completion_tokens_details")
        val completionTokensDetails: CompletionTokensDetails? = null // Made optional
    ) {
        @Serializable
        data class PromptTokensDetails(
            @SerialName("cached_tokens")
            val cachedTokens: Int,
            @SerialName("audio_tokens")
            val audioTokens: Int
        )

        @Serializable
        data class CompletionTokensDetails(
            @SerialName("reasoning_tokens")
            val reasoningTokens: Int,
            @SerialName("audio_tokens")
            val audioTokens: Int,
            @SerialName("accepted_prediction_tokens")
            val acceptedPredictionTokens: Int,
            @SerialName("rejected_prediction_tokens")
            val rejectedPredictionTokens: Int
        )
    }

    val response: String
        get() = choices.firstOrNull()?.message?.content ?: ""
}