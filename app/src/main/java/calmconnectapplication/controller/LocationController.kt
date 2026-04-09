package calmconnectapplication.controller

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng
import calmconnectapplication.model.CalmPlace

interface LocationController {
    fun requestCurrentLocation(callback: (LatLng) -> Unit)
    fun findNearbyPlaces(latLng: LatLng, radiusMeters: Int): LiveData<List<CalmPlace>>
    fun searchPlaces(query: String, latLng: LatLng): List<CalmPlace>
}
