package com.avengers.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

/** Wraps Android SpeechRecognizer + TextToSpeech. Create and use on the main thread. */
class VoiceEngine(private val ctx: Context) {
    val level = MutableStateFlow(0f)
    val speaking = MutableStateFlow(false)

    private var recognizer: SpeechRecognizer? = null
    private var ttsReady = false
    private val tts: TextToSpeech = TextToSpeech(ctx) { status -> ttsReady = status == TextToSpeech.SUCCESS }

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { speaking.value = true }
            override fun onDone(utteranceId: String?) { speaking.value = false }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { speaking.value = false }
        })
    }

    fun listen(lang: String, onResult: (String) -> Unit, onFail: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(ctx)) {
            onFail("Is phone me speech recognition available nahi hai (Google app / Speech Services install karo).")
            return
        }
        stopListening()
        val r = SpeechRecognizer.createSpeechRecognizer(ctx)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) { level.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f) }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { level.value = 0f }
            override fun onError(error: Int) { level.value = 0f; onFail(describe(error)) }
            override fun onResults(results: Bundle?) {
                level.value = 0f
                val t = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (t.isNullOrBlank()) onFail("Kuch sunai nahi diya, phir bolo.") else onResult(t)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        r.startListening(i)
    }

    fun stopListening() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        level.value = 0f
    }

    fun speak(text: String, lang: String) {
        if (!ttsReady) return
        val spoken = clean(text)
        if (spoken.isBlank()) return
        tts.language = Locale.forLanguageTag(lang)
        tts.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "tony")
    }

    fun stopSpeaking() { tts.stop(); speaking.value = false }

    fun shutdown() { stopListening(); tts.stop(); tts.shutdown() }

    private fun clean(t: String) = t
        .replace(Regex("```.*?```", RegexOption.DOT_MATCHES_ALL), " code screen par hai ")
        .replace(Regex("[*#_`>]"), "")
        .trim()

    private fun describe(code: Int) = when (code) {
        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Kuch sunai nahi diya, phir bolo."
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition ke liye network issue."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mic permission nahi hai."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy hai, ek second ruko."
        else -> "Voice recognition error ($code)."
    }
}
