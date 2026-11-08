package com.avengers.ai.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class Mode { CHAT, CODE, VOICE }

data class Message(val role: String, val text: String, val time: Long = System.currentTimeMillis())

data class Session(
    val id: String,
    val mode: Mode,
    val title: String,
    val messages: List<Message>,
    val updated: Long
)

/** Local chat history, stored as a private JSON file inside the app's sandbox. */
class ChatStore(ctx: Context) {
    private val file = File(ctx.filesDir, "chats.json")
    private val _sessions = MutableStateFlow(load())
    val sessions: StateFlow<List<Session>> = _sessions

    @Synchronized
    fun create(mode: Mode): Session {
        val s = Session(UUID.randomUUID().toString(), mode, "New chat", emptyList(), System.currentTimeMillis())
        update { it + s }
        return s
    }

    fun latest(mode: Mode): Session? = _sessions.value.filter { it.mode == mode }.maxByOrNull { it.updated }

    fun get(id: String): Session? = _sessions.value.firstOrNull { it.id == id }

    @Synchronized
    fun append(id: String, m: Message) {
        update { list ->
            list.map {
                if (it.id != id) it else it.copy(
                    messages = it.messages + m,
                    updated = m.time,
                    title = if (it.messages.isEmpty() && m.role == "user") m.text.take(40) else it.title
                )
            }
        }
    }

    @Synchronized fun delete(id: String) = update { list -> list.filter { it.id != id } }

    @Synchronized fun clearAll() = update { emptyList() }

    private fun update(f: (List<Session>) -> List<Session>) {
        _sessions.value = f(_sessions.value)
        save()
    }

    private fun save() {
        try {
            val arr = JSONArray()
            _sessions.value.forEach { s ->
                arr.put(JSONObject().apply {
                    put("id", s.id); put("mode", s.mode.name); put("title", s.title); put("updated", s.updated)
                    put("messages", JSONArray().apply {
                        s.messages.forEach { m ->
                            put(JSONObject().put("role", m.role).put("text", m.text).put("time", m.time))
                        }
                    })
                })
            }
            file.writeText(arr.toString())
        } catch (_: Exception) { /* history is best-effort */ }
    }

    private fun load(): List<Session> = try {
        if (!file.exists()) emptyList() else {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val ms = o.getJSONArray("messages")
                Session(
                    o.getString("id"),
                    Mode.valueOf(o.getString("mode")),
                    o.getString("title"),
                    (0 until ms.length()).map { j ->
                        val m = ms.getJSONObject(j)
                        Message(m.getString("role"), m.getString("text"), m.getLong("time"))
                    },
                    o.getLong("updated")
                )
            }
        }
    } catch (_: Exception) { emptyList() }
}
