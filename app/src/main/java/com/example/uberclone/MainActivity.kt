package com.example.uberclone

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uberclone.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var suggestionAdapter: LocationSuggestionAdapter

    // "PICKUP" or "DROP" to know which search view is active
    private var activeSearchType = "PICKUP"
    private var ignoreTextChange = false
    private var preFilledAddress: String? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        enableEdgeToEdge()
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize MapView
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        // Set up RecyclerView for suggestions
        suggestionAdapter = LocationSuggestionAdapter(emptyList()) { address ->
            val query = address.getAddressLine(0) ?: "Location"
            preFilledAddress = query
            ignoreTextChange = true

            // Clear any previous markers, add new marker, and animate camera.
            googleMap.clear()
            val latLng = LatLng(address.latitude, address.longitude)
            googleMap.addMarker(
                MarkerOptions().position(latLng).title(query)
            )
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            // Update the correct SearchView.
            if (activeSearchType == "PICKUP") {
                binding.searchPickup.setQuery(query, false)
                binding.searchPickup.clearFocus()
            } else if (activeSearchType == "DROP") {
                binding.searchDrop.setQuery(query, false)
                binding.searchDrop.clearFocus()
                // For drop location, show the booking bottom sheet.
                showRideDetails()
            }
            // Clear suggestions.
            suggestionAdapter.updateSuggestions(emptyList())
            binding.recyclerViewSuggestions.visibility = View.GONE

            // Reset flag after a short delay.
            binding.recyclerViewSuggestions.postDelayed({
                ignoreTextChange = false
            }, 200)
        }
        binding.recyclerViewSuggestions.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSuggestions.adapter = suggestionAdapter

        // Always-expanded Pickup SearchView.
        binding.searchPickup.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { geoLocate(it, "PICKUP") }
                    clearFocus()
                    binding.recyclerViewSuggestions.visibility = View.GONE
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (ignoreTextChange || newText == preFilledAddress) return false
                    handleSearchTextChange(newText)
                    return true
                }
            })
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) activeSearchType = "PICKUP"
            }
        }

        // Always-expanded Drop SearchView.
        binding.searchDrop.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { geoLocate(it, "DROP") }
                    clearFocus()
                    binding.recyclerViewSuggestions.visibility = View.GONE
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (ignoreTextChange || newText == preFilledAddress) return false
                    handleSearchTextChange(newText)
                    return true
                }
            })
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) activeSearchType = "DROP"
            }
        }
    }

    // Called when the SearchView text changes to fetch suggestions.
    private fun handleSearchTextChange(newText: String?) {
        if (newText.isNullOrEmpty()) {
            suggestionAdapter.updateSuggestions(emptyList())
            binding.recyclerViewSuggestions.visibility = View.GONE
        } else if (newText.length >= 3) {
            lifecycleScope.launch(Dispatchers.IO) {
                val geocoder = Geocoder(this@MainActivity)
                val suggestions: List<Address> = try {
                    geocoder.getFromLocationName(newText, 5) ?: emptyList()
                } catch (e: IOException) {
                    emptyList()
                }
                withContext(Dispatchers.Main) {
                    if (suggestions.isNotEmpty()) {
                        suggestionAdapter.updateSuggestions(suggestions)
                        binding.recyclerViewSuggestions.visibility = View.VISIBLE
                    } else {
                        suggestionAdapter.updateSuggestions(emptyList())
                        binding.recyclerViewSuggestions.visibility = View.GONE
                    }
                }
            }
        }
    }

    // Geocodes a query, updates the map, and—for drop—shows the booking bottom sheet.
    private fun geoLocate(query: String, type: String) {
        binding.recyclerViewSuggestions.visibility = View.GONE
        val geocoder = Geocoder(this)
        try {
            val addresses: List<Address> = geocoder.getFromLocationName(query, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(address.getAddressLine(0) ?: "Location")
                )
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                if (type == "DROP") {
                    showRideDetails()
                }
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error retrieving location", Toast.LENGTH_SHORT).show()
        }
    }

    // Show the bottom sheet with ride details by passing the selected pickup and drop locations.
    private fun showRideDetails() {
        // Get values from SearchViews. In a real app, fare can be calculated.
        val pickupText = binding.searchPickup.query.toString().ifEmpty { "Current Location" }
        val dropText = binding.searchDrop.query.toString().ifEmpty { "Drop Location" }
        val fareText = "120.00"  // Dummy fare value
        val rideDetailsFragment = RideDetailsFragment.newInstance(pickupText, dropText, fareText)
        rideDetailsFragment.show(supportFragmentManager, rideDetailsFragment.tag)
    }

    // Enables "My Location" on the map and pre-fills the pickup SearchView using reverse geocoding.
    private fun enableMyLocation() {
        try {
            googleMap.isMyLocationEnabled = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                    val geocoder = Geocoder(this)
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val addresses: List<Address> =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                    ?: emptyList()
                            if (addresses.isNotEmpty()) {
                                val currentAddress =
                                    addresses[0].getAddressLine(0) ?: "Current Location"
                                withContext(Dispatchers.Main) {
                                    preFilledAddress = currentAddress
                                    ignoreTextChange = true
                                    binding.searchPickup.setQuery(currentAddress, false)
                                    binding.searchPickup.clearFocus()
                                    binding.recyclerViewSuggestions.visibility = View.GONE
                                    binding.searchPickup.postDelayed({
                                        ignoreTextChange = false
                                    }, 200)
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    Toast.makeText(this, "Unable to fetch current location", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}