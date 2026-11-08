package com.avengers.ai.ai

import com.avengers.ai.data.ChatStore
import com.avengers.ai.data.Message
import com.avengers.ai.data.Mode
import com.avengers.ai.data.SettingsStore
import com.avengers.ai.phone.ActionExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Reply(val display: String, val spoken: String)

class ChatRepository(
    private val settings: SettingsStore,
    private val store: ChatStore,
    private val provider: AiProvider,
    private val actions: ActionExecutor
) {
    suspend fun send(sessionId: String, text: String, mode: Mode): Result<Reply> {
        store.append(sessionId, Message("user", text))
        val history = (store.get(sessionId)?.messages ?: emptyList())
            .takeLast(30)
            .map { ChatTurn(it.role, it.text) }
            .dropWhile { it.role != "user" }

        val raw = provider.generate(TonyPersona.system(settings.persona, mode), history)
            .getOrElse { return Result.failure(it) }

        val (clean, acts) = actions.parse(raw)
        val results = withContext(Dispatchers.Main) { acts.take(3).map { actions.execute(it) } }

        val display = buildString {
            append(clean)
            if (results.isNotEmpty()) {
                append("\n\n")
                append(results.joinToString("\n") { "⚙ $it" })
            }
        }.trim()
        store.append(sessionId, Message("model", display))
        val spoken = (listOf(clean) + results).filter { it.isNotBlank() }.joinToString(". ")
        return Result.success(Reply(display, spoken))
    }
}
