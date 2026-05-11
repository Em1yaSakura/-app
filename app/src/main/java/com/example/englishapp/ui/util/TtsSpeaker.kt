package com.example.englishapp.ui.util

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import java.net.URLEncoder

/**
 * TTS speaker powered by Youdao Dict voice API.
 *
 * API: https://dict.youdao.com/dictvoice?audio={word}&type={1|2}
 * type=1 → US English, type=2 → UK English
 */
class TtsSpeaker(private val context: Context) {
    private var currentPlayer: MediaPlayer? = null
    private var accent: Int = 1 // 1=美音, 2=英音

    fun setAccent(us: Boolean) {
        accent = if (us) 1 else 2
    }

    /**
     * Speak an English word using Youdao's voice API.
     * Falls back silently if the network request fails.
     */
    fun speak(text: String) {
        releasePlayer()
        val encoded = URLEncoder.encode(text.trim(), "UTF-8")
        val url = "https://dict.youdao.com/dictvoice?audio=$encoded&type=$accent"
        try {
            val player = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { start() }
                setOnCompletionListener { release() }
                setOnErrorListener { _, _, _ -> release(); true }
                prepareAsync()
            }
            currentPlayer = player
        } catch (_: Exception) {
            // Network unavailable or URL invalid
        }
    }

    /** Stop and release any current playback. */
    private fun releasePlayer() {
        try {
            currentPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) { }
        currentPlayer = null
    }

    fun shutdown() {
        releasePlayer()
    }
}

@Composable
fun rememberTtsSpeaker(context: Context): TtsSpeaker {
    val speaker = remember { TtsSpeaker(context) }
    DisposableEffect(Unit) {
        onDispose { speaker.shutdown() }
    }
    return speaker
}
