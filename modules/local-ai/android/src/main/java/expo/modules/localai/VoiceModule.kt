package expo.modules.localai

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.content.Intent
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.util.Locale

/** Android voice bridge for Svetlana. Recognition prefers offline engines when available. */
class VoiceModule : Module() {
  private var recognizer: SpeechRecognizer? = null
  private var tts: TextToSpeech? = null

  override fun definition() = ModuleDefinition {
    Name("SvetlanaVoice")

    AsyncFunction("capabilities") {
      val context = appContext.reactContext ?: return@AsyncFunction mapOf("supported" to false)
      mapOf(
        "supported" to SpeechRecognizer.isRecognitionAvailable(context),
        "offlinePreferred" to true,
        "ttsAvailable" to (TextToSpeech(context) { }.let { it.shutdown(); true }),
        "language" to "ru-RU",
      )
    }

    AsyncFunction("speak") { text: String ->
      val context = appContext.reactContext ?: return@AsyncFunction false
      if (tts == null) {
        tts = TextToSpeech(context) { status ->
          if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("ru", "RU")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "svetlana")
          }
        }
      } else {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "svetlana")
      }
      true
    }

    AsyncFunction("stopSpeaking") {
      tts?.stop()
      true
    }

    OnDestroy {
      recognizer?.destroy()
      recognizer = null
      tts?.shutdown()
      tts = null
    }
  }
}
