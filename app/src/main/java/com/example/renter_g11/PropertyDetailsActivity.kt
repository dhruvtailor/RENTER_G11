package com.example.renter_g11

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.renter_g11.databinding.ActivityPropertyDetailsBinding
import com.example.renter_g11.models.Property
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PropertyDetailsActivity : AppCompatActivity() {

    private lateinit var property: Property
    private val TAG: String = "RENTER_APP"
    private lateinit var binding: ActivityPropertyDetailsBinding
    private lateinit var auth: FirebaseAuth

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val propertyId = intent.getStringExtra("PROPERTY_ID").toString()
        val showAddButton = intent.getBooleanExtra("SHOW_ADD_BUTTON",true)

        if(showAddButton) {
            binding.btnAddToWatchlist.visibility = View.VISIBLE
            loadData(propertyId)
        } else {
            binding.btnAddToWatchlist.visibility = View.GONE
            loadWatchlistPropertyData(propertyId)
            Log.d(TAG,propertyId)
        }

        loadData(propertyId)

        binding.btnAddToWatchlist.setOnClickListener {
            if (auth.currentUser != null) {
                checkPropertyExistsInWatchList(property)
            } else {
                Toast.makeText(this, "Please login to add property to watchlist", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun loadData(propertyId: String) {
        Log.d("TESTING", "Loading data")

        db.collection("properties")
            .document(propertyId)
            .get()
            .addOnSuccessListener { result: DocumentSnapshot  ->
                if (result.exists()) {
                    val propertyFromDB: Property? = result.toObject(Property::class.java)
                    if (propertyFromDB != null) {
                        property = propertyFromDB
                        updateTextView()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TESTING", "Error getting documents.", exception)
            }
    }

    private fun loadWatchlistPropertyData(propertyId: String) {
        Log.d("TESTING", "Loading data")

        val uid = auth.currentUser?.uid.toString()

        db.collection("users")
            .document(uid)
            .collection("WatchList")
            .document(propertyId)
            .get()
            .addOnSuccessListener { result: DocumentSnapshot  ->
                if (result.exists()) {
                    val propertyFromDB: Property? = result.toObject(Property::class.java)
                    if (propertyFromDB != null) {
                        property = propertyFromDB
                        updateTextView()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TESTING", "Error getting documents.", exception)
            }
    }

    private fun checkPropertyExistsInWatchList(property: Property) {
        val uid = auth.currentUser?.uid.toString()

        db.collection("users")
            .document(uid)
            .collection("WatchList")
            .whereEqualTo("propertyId", property.id)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    addPropertyToWatchList(uid, property)
                } else {
                    Toast.makeText(this, "Property already in watchlist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TESTING", "Error checking for duplicates", exception)
            }
    }

    private fun addPropertyToWatchList(uid: String, property: Property) {

        val dataToInsert: MutableMap<String, Any> = HashMap()
        dataToInsert["propertyId"] = property.id
        dataToInsert["address"] = property.address
        dataToInsert["imageUrl"] = property.imageUrl
        dataToInsert["isAvailable"] = property.isAvailable
        dataToInsert["rentalType"] = property.rentalType
        dataToInsert["rentalPrice"] = property.rentalPrice
        dataToInsert["latitude"] = property.latitude
        dataToInsert["longitude"] = property.longitude
        dataToInsert["numberOfBedrooms"] = property.numberOfBedrooms
        dataToInsert["landlordId"] = property.landlordID


        db.collection("users")
            .document(uid)
            .collection("WatchList")
            .add(dataToInsert)
            .addOnSuccessListener { docRef ->
                Toast.makeText(this, "Added to Watchlist", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add to Watchlist: $e", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun updateTextView(): Unit {
        binding.tvAddress.text = "Address: " + property.address

        if(property.isAvailable) {
            binding.tvAvailability.text = "Status: Available"
        } else {
            binding.tvAvailability.text = "Status: Not Available"
        }

        binding.tvNumBedroom.text = "Bedrooms: " + property.numberOfBedrooms.toString()
        binding.tvRentalPrice.text = "Rental Price: " + property.rentalPrice.toString()
        binding.tvRentalType.text = "Rental Type: " + property.rentalType.toString()

        val imageUrl = property.imageUrl
        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivPropertyImage)
    }
}