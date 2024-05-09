package com.example.renter_g11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renter_g11.adapters.WatchListAdapter
import com.example.renter_g11.databinding.ActivityWatchListBinding
import com.example.renter_g11.models.Property
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class WatchListActivity : AppCompatActivity() {

    private lateinit var uid: String
    private val TAG: String = "RENTER_APP"
    private lateinit var binding: ActivityWatchListBinding
    private lateinit var auth: FirebaseAuth
    lateinit var watchListAdapter: WatchListAdapter

    val db = Firebase.firestore

    var watchList: MutableList<Property> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        watchListAdapter = WatchListAdapter(watchList, deleteRowButtonClicked, goToPropertyDetails)
        binding.rv.adapter = watchListAdapter
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

        uid = auth.currentUser?.uid.toString()

        loadData()
    }

    private fun loadData() {

        db.collection("users")
            .document(uid)
            .collection("WatchList")
            .get()
            .addOnSuccessListener { results: QuerySnapshot ->
                watchList.clear()
                for (document in results) {
                    val userWatchListFromDB: Property = document.toObject(Property::class.java)
                    watchList.add(userWatchListFromDB)
                }
                watchListAdapter.notifyDataSetChanged()

            }.addOnFailureListener { e ->
                Log.d("WatchListActivity", "Error fetching user profile: ${e.message}", e)
                Toast.makeText(this, "Error loading profile.", Toast.LENGTH_LONG).show()
            }
    }

    val deleteRowButtonClicked: (String) -> Unit = { propertyID: String
        ->
        db.collection("users")
            .document(uid)
            .collection("WatchList")
            .document(propertyID)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Document deleted!")
                loadData()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document", e)
            }
    }


    val goToPropertyDetails: (Int) -> Unit = { position ->
        val property = watchList[position]
        val intent = Intent(this@WatchListActivity, PropertyDetailsActivity::class.java)
        intent.putExtra("PROPERTY_ID", property.id)
        intent.putExtra("SHOW_ADD_BUTTON", false)
        startActivity(intent)
    }

}