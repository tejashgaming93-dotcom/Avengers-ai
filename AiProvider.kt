package com.avengers.ai.ai

import com.avengers.ai.data.SettingsStore

data class ChatTurn(val role: String, val text: String) // role: "user" | "model"

class AiException(message: String) : Exception(message)

/** The UI never talks to Gemini directly. Add a new provider by implementing this. */
interface AiProvider {
    val id: String
    suspend fun generate(system: String, history: List<ChatTurn>): Result<String>
    suspend fun test(): Result<String>
}

object ProviderFactory {
    val AVAILABLE = listOf("gemini" to "Google Gemini")

    fun create(settings: SettingsStore): AiProvider = when (settings.provider) {
        else -> GeminiProvider(settings)
    }
}
