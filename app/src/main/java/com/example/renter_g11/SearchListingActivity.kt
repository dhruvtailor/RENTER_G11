package com.example.renter_g11

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renter_g11.adapters.PropertyListingAdapter
import com.example.renter_g11.databinding.ActivitySearchListingBinding
import com.example.renter_g11.models.Property
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchListingActivity : AppCompatActivity(), OnMapReadyCallback, OnMarkerClickListener {

    private val TAG: String = "RENTER_APP"
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var binding: ActivitySearchListingBinding
    private lateinit var mMap: GoogleMap
    private var isMapInitialized: Boolean = false
    private val LOCATION_PERMISSION_REQUEST = 1

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    lateinit var propertyListingAdapter: PropertyListingAdapter
    var propertyList: MutableList<Property> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar)

        auth = Firebase.auth

        // Initialize map fragment
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        propertyListingAdapter = PropertyListingAdapter(propertyList, goToPropertyDetails)

        binding.rvPropertyListing.adapter = propertyListingAdapter
        binding.rvPropertyListing.layoutManager = LinearLayoutManager(this)
        binding.rvPropertyListing.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

        binding.btnSearch.setOnClickListener {
            if(binding.etPrice.text.isNotEmpty()) {
                var price: Double = binding.etPrice.text.toString().toDouble()
                filterData(price)
            } else {
                loadData()
            }
        }
        checkLocationPermission()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)

        isMapInitialized = true

        loadData()

        setupMapUI()
    }

    fun loadData() {
        Log.d(TAG, "Loading data")

        mMap.clear()

        var markerPoints: MutableList<Marker?> = mutableListOf()

        val toronto = LatLng(43.7038407,-79.4153924)

        propertyList.clear()

        db.collection("properties")
            .whereEqualTo("isAvailable",true)
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                val propertyListFromDB: MutableList<Property> = mutableListOf()
                for (document: QueryDocumentSnapshot in result) {
                    val propertyFromDB: Property = document.toObject(Property::class.java)
                    propertyList.add(propertyFromDB)
                    var latLng = LatLng(propertyFromDB.latitude,propertyFromDB.longitude)
                    var marker = mMap.addMarker(MarkerOptions().position(latLng).title(propertyFromDB.rentalType))
                    marker?.tag = propertyFromDB
                    markerPoints.add(marker)
                }

                propertyListingAdapter.notifyDataSetChanged()

                // Adjust the camera to show all markers
                val bounds = LatLngBounds.Builder().apply {
                    markerPoints.forEach {
                        if (it != null) {
                            include(LatLng(it.position.latitude,it.position.longitude))
                        }
                    }
                }.build()

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 11.5f))

                Log.d(TAG, "Number of items retrieved from Firestore: ${propertyListFromDB.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error retrieving documents", exception)
            }
    }

    fun filterData(price: Double) {
        Log.d(TAG, "Loading data")

        var markerPoints: MutableList<Marker?> = mutableListOf()

        mMap.clear()

        val toronto = LatLng(43.7038407,-79.4153924)

        propertyList.clear()

        db.collection("properties")
            .whereEqualTo("isAvailable",true)
            .whereLessThanOrEqualTo("rentalPrice",price)
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                val propertyListFromDB: MutableList<Property> = mutableListOf()
                for (document: QueryDocumentSnapshot in result) {
                    val propertyFromDB: Property = document.toObject(Property::class.java)
                    propertyList.add(propertyFromDB)
                    var latLng = LatLng(propertyFromDB.latitude,propertyFromDB.longitude)
                    var marker = mMap.addMarker(MarkerOptions().position(latLng).title(propertyFromDB.rentalType))
                    marker?.tag = propertyFromDB
                    markerPoints.add(marker)
                }

                propertyListingAdapter.notifyDataSetChanged()

                // Adjust the camera to show all markers
                val bounds = LatLngBounds.Builder().apply {
                    markerPoints.forEach {
                        if (it != null) {
                            include(LatLng(it.position.latitude,it.position.longitude))
                        }
                    }
                }.build()

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 11.5f))

                Log.d(TAG, "Number of items retrieved from Firestore: ${propertyListFromDB.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error retrieving documents", exception)
            }
    }

    private fun setupMapUI() {
        // This function encapsulates UI settings for the map.
        mMap.uiSettings.isZoomControlsEnabled = true
        updateLocationUI()
    }

    private fun checkLocationPermission() {
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        } else {
            // Permissions have already been granted; set up the location UI
            if (isMapInitialized)
                updateLocationUI()
        }
    }

    private fun updateLocationUI() {
        // Update UI based on location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        } else {
            mMap.isMyLocationEnabled = false
            mMap.uiSettings.isMyLocationButtonEnabled = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocationUI()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_show_map -> {
                if(!mapFragment.isVisible) {
                    loadData()
                    mapFragment.view?.visibility = View.VISIBLE
                    binding.rvPropertyListing.visibility = View.GONE
                }
                true
            }
            R.id.mi_show_list -> {
                if(!binding.rvPropertyListing.isVisible) {
                    loadData()
                    binding.rvPropertyListing.visibility = View.VISIBLE
                    mapFragment.view?.visibility = View.GONE
                }
                true
            }
            R.id.mi_my_watchlist -> {
                if (auth.currentUser == null) {
                    val snackbar = Snackbar.make(binding.root, "You have to be logged in to access watchlist", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    false
                } else {
                    val intent = Intent(this@SearchListingActivity, WatchListActivity::class.java)
                    startActivity(intent)
                    true
                }
            }
            R.id.mi_login -> {
                if (auth.currentUser != null) {
                    val snackbar = Snackbar.make(binding.root, "User already logged in", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    false
                } else {
                    val intent = Intent(this@SearchListingActivity, LoginActivity::class.java)
                    startActivity(intent)
                    true
                }
            }
            R.id.mi_logout -> {
                if (auth.currentUser == null) {
                    val snackbar = Snackbar.make(binding.root, "Cannot logout because there is no logged in user", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    false
                } else {
                    Firebase.auth.signOut()
                    val snackbar = Snackbar.make(binding.root, "User logged out", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    true
                }
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    val goToPropertyDetails: (Int) -> Unit = { position ->
        val property = propertyList[position]
        val intent = Intent(this@SearchListingActivity, PropertyDetailsActivity::class.java)
        intent.putExtra("PROPERTY_ID", property.id)
        intent.putExtra("SHOW_ADD_BUTTON", true)
        startActivity(intent)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG,"CLICKED")
        val property = marker.tag as Property
        val intent = Intent(this@SearchListingActivity, PropertyDetailsActivity::class.java)
        intent.putExtra("PROPERTY_ID", property.id)
        intent.putExtra("SHOW_ADD_BUTTON", true)
        startActivity(intent)
        return true
    }
}