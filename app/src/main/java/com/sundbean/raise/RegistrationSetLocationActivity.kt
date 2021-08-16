package com.sundbean.raise

import android.content.ContentValues.TAG
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sundbean.raise.BuildConfig.MAPS_API_KEY
import java.util.*

class RegistrationSetLocationActivity : AppCompatActivity() {

    private lateinit var autocompleteFragment : AutocompleteSupportFragment
    private lateinit var setLocationBtn : Button
    private lateinit var auth: FirebaseAuth
    private lateinit var coordinates : LatLng
    private var selectedPlaceId: String? = null
    private var TAG = "RegistrationSetLocationActivity"

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
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                // using the place id: https://developers.google.com/maps/documentation/places/web-service/place-id
                selectedPlaceId = place.id
                coordinates = place.latLng!!
                Log.i(TAG, "Place: ${place.name}, ${place.id}, coordinates: $coordinates")
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })

        //TODO: find a way to hide this api key!
        Places.initialize(applicationContext, MAPS_API_KEY)
        val placesClient = Places.createClient(this)

        setLocationBtn.setOnClickListener {
            storeLocationInFirebase(coordinates)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun storeLocationInFirebase(coordinates: LatLng) {
        val db = Firebase.firestore
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No signed in user", Toast.LENGTH_SHORT).show()
        }

        var geocoder = Geocoder(this@RegistrationSetLocationActivity, Locale.US)
        val addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
        var locationData = hashMapOf(
            "address" to addresses[0].getAddressLine(0),
            "coordinates" to coordinates,
            "placeId" to selectedPlaceId,
            "locality" to addresses[0].locality,
            "admin" to addresses[0].adminArea,
            "subAdmin" to addresses[0].subAdminArea,
            "postalCode" to addresses[0].postalCode)

        // Find the user document of the current user
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .update("location", locationData)
        }
    }



}