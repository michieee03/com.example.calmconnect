package calmconnectapplication.controller.impl

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import calmconnectapplication.controller.LocationController
import calmconnectapplication.model.CalmPlace
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class LocationControllerImpl(
    private val context: Context,
    private val placesClient: PlacesClient
) : LocationController {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var cachedPlaces: List<CalmPlace> = emptyList()
    private var cacheTimestamp: Long = 0L

    private val nearbyPlacesLiveData = MutableLiveData<List<CalmPlace>>()

    companion object {
        private const val TAG = "LocationControllerImpl"
        private val CALM_PLACE_TYPES =
            listOf("park", "library", "spa", "natural_feature", "campground")
        private val DATE_FORMAT =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        private val PLACE_FIELDS = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.TYPES,
            Place.Field.RATING
        )

        fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val radius = 6371000.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)

            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) *
                    cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2)

            return radius * 2 * atan2(sqrt(a), sqrt(1 - a))
        }
    }

    @SuppressLint("MissingPermission")
    override fun requestCurrentLocation(callback: (LatLng) -> Unit) {
        if (!hasLocationPermission()) {
            showPermissionRationaleDialog(onManualSearch = null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    callback(LatLng(location.latitude, location.longitude))
                } else {
                    Log.w(TAG, "Last known location is null, requesting fresh location")
                    requestFreshLocation(callback)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get location", e)
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(callback: (LatLng) -> Unit) {
        if (!hasLocationPermission()) return

        val request = com.google.android.gms.location.CurrentLocationRequest.Builder()
            .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    callback(LatLng(location.latitude, location.longitude))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get fresh location", e)
            }
    }

    override fun findNearbyPlaces(latLng: LatLng, radiusMeters: Int): LiveData<List<CalmPlace>> {
        if (!hasLocationPermission()) {
            showPermissionRationaleDialog(onManualSearch = null)

            if (cachedPlaces.isNotEmpty()) {
                nearbyPlacesLiveData.postValue(cachedPlaces)
            }

            return nearbyPlacesLiveData
        }

        scope.launch(Dispatchers.IO) {
            try {
                val allPlaces = mutableListOf<CalmPlace>()

                for (placeType in CALM_PLACE_TYPES) {
                    try {
                        val request = SearchNearbyRequest.builder(
                            com.google.android.libraries.places.api.model.CircularBounds.newInstance(
                                latLng,
                                radiusMeters.toDouble()
                            ),
                            PLACE_FIELDS
                        )
                            .setIncludedTypes(listOf(placeType))
                            .setMaxResultCount(10)
                            .build()

                        val response = placesClient.searchNearby(request).await()

                        val places = response.places.mapNotNull { place ->
                            place.toCalmPlace()
                        }

                        allPlaces.addAll(places)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to fetch places for type=$placeType", e)
                    }
                }

                val deduplicated = allPlaces.distinctBy { it.placeId }

                val withinRadius = deduplicated.filter { place ->
                    distanceMeters(
                        latLng.latitude,
                        latLng.longitude,
                        place.latLng.latitude,
                        place.latLng.longitude
                    ) <= radiusMeters
                }

                val sorted = withinRadius.sortedBy { place ->
                    distanceMeters(
                        latLng.latitude,
                        latLng.longitude,
                        place.latLng.latitude,
                        place.latLng.longitude
                    )
                }

                cachedPlaces = sorted
                cacheTimestamp = System.currentTimeMillis()

                nearbyPlacesLiveData.postValue(sorted)

            } catch (e: Exception) {
                Log.e(TAG, "findNearbyPlaces failed", e)
                handleApiFailure()
            }
        }

        return nearbyPlacesLiveData
    }

    override fun searchPlaces(query: String, latLng: LatLng): List<CalmPlace> {
        if (query.isBlank()) return emptyList()

        val results = mutableListOf<CalmPlace>()

        val latDelta = 0.45
        val lngDelta = 0.45

        val bounds = RectangularBounds.newInstance(
            LatLng(latLng.latitude - latDelta, latLng.longitude - lngDelta),
            LatLng(latLng.latitude + latDelta, latLng.longitude + lngDelta)
        )

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setLocationBias(bounds)
            .build()

        try {
            val response = com.google.android.gms.tasks.Tasks.await(
                placesClient.findAutocompletePredictions(request)
            )

            for (prediction in response.autocompletePredictions) {
                val placeId = prediction.placeId
                val name = prediction.getPrimaryText(null).toString()
                val address = prediction.getSecondaryText(null).toString()

                results.add(
                    CalmPlace(
                        placeId = placeId,
                        name = name,
                        address = address,
                        latLng = latLng,
                        type = "search_result",
                        rating = null
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchPlaces failed", e)
        }

        return results
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun showPermissionRationaleDialog(onManualSearch: (() -> Unit)?) {
        try {
            AlertDialog.Builder(context)
                .setTitle("Location Permission Required")
                .setMessage(
                    "Calm Connect needs your location to find peaceful places nearby. " +
                            "Without this permission you can still search manually."
                )
                .setPositiveButton("Search Manually") { _, _ ->
                    onManualSearch?.invoke()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.w(TAG, "Could not show rationale dialog", e)
        }
    }

    private fun handleApiFailure() {
        if (cachedPlaces.isNotEmpty()) {
            val lastUpdated = DATE_FORMAT.format(Date(cacheTimestamp))
            Log.i(TAG, "Showing cached results. Last updated: $lastUpdated")
            nearbyPlacesLiveData.postValue(cachedPlaces)
        } else {
            nearbyPlacesLiveData.postValue(emptyList())
        }
    }

    private fun Place.toCalmPlace(): CalmPlace? {
        val id = this.id ?: return null
        val name = this.name ?: return null
        val latLng = this.latLng ?: return null
        val address = this.address ?: ""
        val type = this.placeTypes?.firstOrNull()?.toString()?.lowercase() ?: "place"
        val rating = this.rating?.toFloat()

        return CalmPlace(
            placeId = id,
            name = name,
            address = address,
            latLng = latLng,
            type = type,
            rating = rating
        )
    }
}