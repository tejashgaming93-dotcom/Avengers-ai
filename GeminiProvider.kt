package com.avengers.ai.ai

import com.avengers.ai.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Gemini REST client. The key goes in the x-goog-api-key header (not the URL),
 * and nothing here logs request or response bodies.
 */
class GeminiProvider(private val settings: SettingsStore) : AiProvider {
    override val id = "gemini"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun generate(system: String, history: List<ChatTurn>): Result<String> =
        withContext(Dispatchers.IO) {
            val key = settings.apiKey
            if (key.isBlank()) {
                return@withContext Result.failure(AiException("Gemini API key set nahi hai. Settings me jaake daalo."))
            }
            try {
                val body = JSONObject().apply {
                    put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
                    put("contents", JSONArray().apply {
                        history.forEach { t ->
                            put(JSONObject().put("role", t.role)
                                .put("parts", JSONArray().put(JSONObject().put("text", t.text))))
                        }
                    })
                }
                val req = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/${settings.model}:generateContent")
                    .header("x-goog-api-key", key)
                    .post(body.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val result: Result<String> = client.newCall(req).execute().use { resp ->
                    val raw = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) Result.failure(AiException(friendly(resp.code, raw)))
                    else parse(raw)
                }
                result
            } catch (e: IOException) {
                Result.failure(AiException("Network issue hai. Internet check karo aur dobara try karo."))
            } catch (e: Exception) {
                Result.failure(AiException("Kuch gadbad ho gayi: ${e.javaClass.simpleName}"))
            }
        }

    override suspend fun test(): Result<String> =
        generate("Reply with exactly one word: OK", listOf(ChatTurn("user", "ping")))
            .map { "Connected. Model: ${settings.model}" }

    private fun parse(raw: String): Result<String> {
        val root = JSONObject(raw)
        val cands = root.optJSONArray("candidates")
        if (cands == null || cands.length() == 0) {
            val reason = root.optJSONObject("promptFeedback")?.optString("blockReason").orEmpty()
            return Result.failure(AiException(
                if (reason.isNotBlank()) "Gemini ne ye request block kar di ($reason)." else "Gemini ne khaali reply diya."
            ))
        }
        val parts = cands.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
        val text = buildString {
            if (parts != null) for (i in 0 until parts.length()) append(parts.getJSONObject(i).optString("text"))
        }.trim()
        return if (text.isBlank()) Result.failure(AiException("Gemini ne khaali reply diya. Dobara try karo."))
        else Result.success(text)
    }

    private fun friendly(code: Int, raw: String): String {
        val detail = try { JSONObject(raw).optJSONObject("error")?.optString("message").orEmpty() } catch (_: Exception) { "" }
        return when {
            code == 400 && detail.contains("API key", true) -> "API key galat hai. Settings me check karo."
            code == 400 -> "Request invalid thi (400). Model name check karo."
            code == 401 || code == 403 -> "API key ko permission nahi hai ($code). Key ya Gemini API access check karo."
            code == 404 -> "Ye model nahi mila. Settings me model badlo."
            code == 429 -> "Rate limit / quota khatam (429). Thodi der baad try karo."
            code >= 500 -> "Gemini server issue ($code). Thodi der baad try karo."
            else -> "Error $code${if (detail.isNotBlank()) ": $detail" else ""}"
        }
    }
}
