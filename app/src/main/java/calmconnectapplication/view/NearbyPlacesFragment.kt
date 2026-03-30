package com.example.calmconnect.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.calmconnect.BuildConfig
import com.example.calmconnect.controller.impl.LocationControllerImpl
import com.example.calmconnect.databinding.FragmentNearbyPlacesBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class NearbyPlacesFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentNearbyPlacesBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationController: LocationControllerImpl
    private lateinit var placesClient: PlacesClient
    private var googleMap: GoogleMap? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            loadNearbyPlaces()
        } else {
            locationController.showPermissionRationaleDialog(null)
            binding.btnRetry.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNearbyPlacesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)
        }
        placesClient = Places.createClient(requireContext())
        locationController = LocationControllerImpl(requireContext(), placesClient)

        val mapFrag = childFragmentManager.findFragmentById(com.example.calmconnect.R.id.mapFragment)
                as SupportMapFragment
        mapFrag.getMapAsync(this)

        binding.btnRetry.setOnClickListener {
            binding.btnRetry.visibility = View.GONE
            checkPermissionsAndLoad()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkPermissionsAndLoad()
    }

    private fun checkPermissionsAndLoad() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            loadNearbyPlaces()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun loadNearbyPlaces() {
        locationController.requestCurrentLocation { latLng ->
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))

            locationController.findNearbyPlaces(latLng, 5000)
                .observe(viewLifecycleOwner) { places ->
                    if (places.isEmpty()) {
                        Toast.makeText(requireContext(), "No calm places found nearby", Toast.LENGTH_SHORT).show()
                        binding.btnRetry.visibility = View.VISIBLE
                        return@observe
                    }
                    googleMap?.clear()
                    places.forEach { place ->
                        googleMap?.addMarker(
                            MarkerOptions()
                                .position(place.latLng)
                                .title(place.name)
                                .snippet(place.address)
                        )
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
