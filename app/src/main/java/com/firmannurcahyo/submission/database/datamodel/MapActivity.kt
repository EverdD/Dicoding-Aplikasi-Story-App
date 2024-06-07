package com.firmannurcahyo.submission.database.datamodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.firmannurcahyo.submission.R
import com.firmannurcahyo.submission.databinding.ActivityMapBinding
import com.firmannurcahyo.submission.frontend.authentication.Resource
import com.firmannurcahyo.submission.frontend.model.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_key")
    private var _binding: ActivityMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private lateinit var mapViewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        setUpViewModel()
        setupMapFragment()
    }

    private fun setupActionBar() {
        supportActionBar?.hide()
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }
        enableMyLocation()
        manyMarker()
        mMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(0.7893, 113.9213)))
    }

    private fun manyMarker() {
        mapViewModel.stories.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val data = resource.data
                    if (data.isNullOrEmpty()) return@observe

                    showLoading(false)
                    data.forEach { stories ->
                        val lat = stories.lat ?: 0.0
                        val lon = stories.lon ?: 0.0
                        if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                            mMap.addMarker(
                                MarkerOptions().position(
                                    LatLng(lat, lon)
                                ).title(
                                    stories.name
                                ).snippet(
                                    getDescription(lat, lon, stories.description ?: "")
                                )
                            )?.tag = stories
                        } else {
                            // Handle out-of-range lat/lon values
                            // For example, skip adding marker or log an error
                        }
                    }
                }
                is Resource.Loading -> showLoading(true)
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            }
        }
    }

    private fun getDescription(lat: Double, lon: Double, description: String): String? {
        val geocoder = Geocoder(this@MapActivity, Locale.getDefault())
        return try {
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (list != null && list.isNotEmpty()) description else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun setUpViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        val factory = ViewModelFactory(pref)
        factory.setApplication(application)
        mapViewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
        fetchData()
    }

    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            mapViewModel.getStories()
        }
    }

    private fun enableMyLocation() {
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        mMap.isMyLocationEnabled = isPermissionGranted

        if (!isPermissionGranted) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showLoading(state: Boolean) {
        binding.pbLoading.visibility = if (state) View.VISIBLE else View.GONE
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        when {
            isGranted -> enableMyLocation()
            else -> handlePermissionDenied()
        }
    }

    private fun handlePermissionDenied() {
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
    }
}