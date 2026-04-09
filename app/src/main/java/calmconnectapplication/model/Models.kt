package calmconnectapplication.model

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

data class BreathingSession(
    val pattern: BreathingPattern,
    val currentPhase: MutableLiveData<String> = MutableLiveData(""),
    val isActive: Boolean = true
)

data class MeditationSession(
    val durationMinutes: Int,
    val remainingSeconds: MutableLiveData<Int> = MutableLiveData(0),
    val isActive: Boolean = true
)

data class GameSession(
    val type: GameType,
    val isActive: Boolean = true
)

data class CalmPlace(
    val placeId: String,
    val name: String,
    val address: String,
    val latLng: LatLng,
    val type: String,
    val rating: Float?
)