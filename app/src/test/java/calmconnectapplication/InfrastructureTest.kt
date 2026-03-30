package com.example.calmconnect

import com.example.calmconnect.model.BreathingPattern
import com.example.calmconnect.model.GameType
import com.example.calmconnect.model.SoundType
import com.example.calmconnect.model.TimerPhase
import com.example.calmconnect.model.TimerState
import com.example.calmconnect.util.Constants
import com.example.calmconnect.util.Result
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InfrastructureTest {

    @Test
    fun `SoundType enum has all required values`() {
        val values = SoundType.values()
        assertTrue(values.contains(SoundType.RAIN_DROPS))
        assertTrue(values.contains(SoundType.OCEAN_WAVES))
        assertTrue(values.contains(SoundType.GENTLE_PIANO))
        assertTrue(values.contains(SoundType.FOREST_AMBIENCE))
        assertEquals(4, values.size)
    }

    @Test
    fun `BreathingPattern enum has all required values`() {
        val values = BreathingPattern.values()
        assertTrue(values.contains(BreathingPattern.BOX_4_4_4_4))
        assertTrue(values.contains(BreathingPattern.RELAXING_4_7_8))
        assertTrue(values.contains(BreathingPattern.ENERGIZING_2_2_4))
        assertEquals(3, values.size)
    }

    @Test
    fun `GameType enum has all required values`() {
        val values = GameType.values()
        assertTrue(values.contains(GameType.TAPPING))
        assertTrue(values.contains(GameType.MEMORY))
        assertTrue(values.contains(GameType.BREATHING))
        assertEquals(3, values.size)
    }

    @Test
    fun `TimerPhase enum has all required values`() {
        val values = TimerPhase.values()
        assertTrue(values.contains(TimerPhase.WORK))
        assertTrue(values.contains(TimerPhase.SHORT_BREAK))
        assertTrue(values.contains(TimerPhase.LONG_BREAK))
        assertEquals(3, values.size)
    }

    @Test
    fun `TimerState data class holds correct values`() {
        val state = TimerState(
            phase = TimerPhase.WORK,
            remainingSeconds = 1500,
            completedPomodoros = 0
        )
        assertEquals(TimerPhase.WORK, state.phase)
        assertEquals(1500, state.remainingSeconds)
        assertEquals(0, state.completedPomodoros)
    }

    @Test
    fun `Result Success wraps value correctly`() {
        val result: Result<String> = Result.Success("ok")
        assertTrue(result is Result.Success)
        assertEquals("ok", (result as Result.Success).data)
    }

    @Test
    fun `Result Error wraps message correctly`() {
        val result: Result<Unit> = Result.Error("something went wrong")
        assertTrue(result is Result.Error)
        assertEquals("something went wrong", (result as Result.Error).message)
    }

    @Test
    fun `VALID_EMOTIONS_SET is non-empty`() {
        assertNotNull(Constants.VALID_EMOTIONS_SET)
        assertTrue(Constants.VALID_EMOTIONS_SET.isNotEmpty())
    }
}
