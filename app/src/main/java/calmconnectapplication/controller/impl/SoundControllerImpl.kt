package com.example.calmconnect.controller.impl

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.calmconnect.controller.SoundController
import com.example.calmconnect.model.SoundType

class SoundControllerImpl(
    private val context: Context,
    private val onPlaybackError: (() -> Unit)? = null
) : SoundController {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // AudioFocusRequest for API 26+
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                releasePlayer()
                onPlaybackError?.invoke()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.start()
            }
        }
    }

    override fun play(soundType: SoundType) {
        // Stop any currently playing sound first
        stop()

        val resId = soundTypeToResId(soundType)

        // Request audio focus
        val focusGranted = requestAudioFocus()
        if (!focusGranted) {
            releasePlayer()
            onPlaybackError?.invoke()
            return
        }

        val player = MediaPlayer.create(context, resId)
        if (player == null) {
            abandonAudioFocus()
            onPlaybackError?.invoke()
            return
        }

        player.isLooping = true
        player.start()
        mediaPlayer = player
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun stop() {
        cancelTimer()
        releasePlayer()
        abandonAudioFocus()
    }

    override fun setTimer(durationMinutes: Int) {
        cancelTimer()
        val runnable = Runnable { stop() }
        timerRunnable = runnable
        handler.postDelayed(runnable, durationMinutes * 60 * 1000L)
    }

    override fun setVolume(level: Float) {
        val clamped = level.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(clamped, clamped)
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    private fun soundTypeToResId(soundType: SoundType): Int {
        return when (soundType) {
            SoundType.RAIN_DROPS -> context.resources.getIdentifier("rain_drops", "raw", context.packageName)
            SoundType.OCEAN_WAVES -> context.resources.getIdentifier("ocean_waves", "raw", context.packageName)
            SoundType.GENTLE_PIANO -> context.resources.getIdentifier("gentle_piano", "raw", context.packageName)
            SoundType.FOREST_AMBIENCE -> context.resources.getIdentifier("forest_ambience", "raw", context.packageName)
        }
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun cancelTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }
}
