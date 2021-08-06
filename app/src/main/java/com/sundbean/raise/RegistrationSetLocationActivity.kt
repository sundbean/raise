package com.sundbean.raise

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

class RegistrationSetLocationActivity : AppCompatActivity() {

    private lateinit var autocompleteFragment : AutocompleteSupportFragment
    private lateinit var setLocationBtn : Button
    private lateinit var auth: FirebaseAuth

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
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })

        //TODO: find a way to hide this api key!
        Places.initialize(applicationContext, "AIzaSyB_A0RKs7JmFRfMvokjaUYPnDvciHNyheU")
        val placesClient = Places.createClient(this)

        setLocationBtn.setOnClickListener {
            auth.signOut()
            // since user is signed out, they need to be directed back to LoginActivity
            val logoutIntent = Intent(this, LoginActivity::class.java)
            // clear the whole backstack
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        }
    }



}