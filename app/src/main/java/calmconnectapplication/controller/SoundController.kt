package com.example.calmconnect.controller

import com.example.calmconnect.model.SoundType

interface SoundController {
    fun play(soundType: SoundType)
    fun pause()
    fun stop()
    fun setTimer(durationMinutes: Int)
    fun setVolume(level: Float)
}
