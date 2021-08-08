package com.sundbean.raise

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class RegistrationSetLocationActivity : AppCompatActivity() {

    private lateinit var autocompleteFragment : AutocompleteSupportFragment
    private lateinit var setLocationBtn : Button
    private lateinit var auth: FirebaseAuth
    private var selectedPlaceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_set_location)

        // grab the user id and email id from the intent that sent you over to this page (from create account activity)
        val userId = intent.getStringExtra("user_id")
        val emailId = intent.getStringExtra("email_id")

        setLocationBtn = findViewById(R.id.btnSetLocation)
        auth = Firebase.auth

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                // using the place id: https://developers.google.com/maps/documentation/places/web-service/place-id
                selectedPlaceId = place.id
                Log.i("LocationActivity", "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("Location Activity", "An error occurred: $status")
            }
        })

        //TODO: find a way to hide this api key!
        Places.initialize(applicationContext, "AIzaSyB_A0RKs7JmFRfMvokjaUYPnDvciHNyheU")
        val placesClient = Places.createClient(this)

        setLocationBtn.setOnClickListener {
            storeLocationInFirebase(selectedPlaceId)
            auth.signOut()
            // since user is signed out, they need to be directed back to LoginActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun storeLocationInFirebase(userPlaceId: String?) {
        val db = Firebase.firestore
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No signed in user", Toast.LENGTH_SHORT).show()
        }
        // Find the user document of the current user
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .update("location", userPlaceId)
        }
    }



}