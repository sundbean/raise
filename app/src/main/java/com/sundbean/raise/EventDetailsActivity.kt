package com.sundbean.raise

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_event_details.*

class EventDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var displayImageView : ImageView
    private var latitude : Double? = null
    private var longitude : Double? = null
    private var TAG = "EventDetailsActivity"

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        displayImageView = findViewById(R.id.ivEventDetailsImage)
        mapView = findViewById(R.id.mapView)

        val intent = getIntent()
        val eventId = intent.getStringExtra("event_id") as String

        //TODO: Find a way to implement this in helper function fillViewsWithDataFromFirestore, so we dont make two queries to firestore in same activity
        Firebase.firestore.collection("events").document(eventId).get().addOnSuccessListener { document ->
            var location = document.get("location") as Map<*, *>
            var coordinates = location.get("coordinates") as Map<String, Double>
            latitude = coordinates["latitude"]
            longitude = coordinates["longitude"]
        }

        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        fillViewsWithDataFromFirestore(eventId)

        ibBackButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            //get rid of any Login or Register Activities running in the background
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }

    }

    private fun fillViewsWithDataFromFirestore(eventId: String) {
        val db = Firebase.firestore
        val eventRef = db.collection("events").document(eventId)
        eventRef.get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "Event data: ${document.data}")
                val imgUrl = document.getString("photoUrl")
                Glide.with(this).load(imgUrl).override(Resources.getSystem().getDisplayMetrics().widthPixels).into(displayImageView)
                tvEventDetailTitle.text = document.getString("name")
                var numAttendees = document.get("rsvpNum")
                tvNumberOfAttendees.text = "$numAttendees going"
                tvEventDetailTime.text = document.getString("time")

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Error getting document reference: $e")
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY) ?: Bundle().also {
            outState.putBundle(MAPVIEW_BUNDLE_KEY, it)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onMapReady(map: GoogleMap) {
        var coordinates = LatLng(latitude!!, longitude!!)
        map.addMarker(MarkerOptions().position(coordinates).title("Marker"))
        var zoomLevel = 16.0f // this goes up to 21
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoomLevel))
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    //TODO: Fix scroll issue like other scroll view layouts

}

data class Attendees(
    var attendeesList: ArrayList<String>
)