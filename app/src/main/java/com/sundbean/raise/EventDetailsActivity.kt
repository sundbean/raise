package com.sundbean.raise

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class EventDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var displayImageView : ImageView
    private var latitude : Double? = null
    private var longitude : Double? = null
    private var TAG = "EventDetailsActivity"

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillViewsWithDataFromFirestore(eventId: String) {
        /**
         * Queries firestore 'events' collection using the [eventId]. Gets values of various fields and displays them in appropriate
         * layout views.
         */
        val db = Firebase.firestore
        val eventRef = db.collection("events").document(eventId)
        eventRef.get()
            .addOnSuccessListener { document ->
                // load event photo
                val imgUrl = document.getString("photoUrl")
                Glide.with(this).load(imgUrl).override(Resources.getSystem().getDisplayMetrics().widthPixels).into(displayImageView)

                tvEventDetailTitle.text = document.getString("name")

                // display number of attendees
                var numAttendees = document.get("rsvpNum")
                tvNumberOfAttendees.text = "$numAttendees going"

                // display event time
                var startTime = formatFirestoreTimeToDisplayString(document.get("startTime") as Map<String, Int?>)
                var endTime = formatFirestoreTimeToDisplayString(document.get("endTime") as Map<String, Int?>)
                tvEventDetailTime.text = "$startTime-$endTime"

                //display event date
                var date : String = formatDateForDisplay(document.getString("date"))
                tvEventDetailDate.text = date

                // display event description
                tvEventDetailDescription.text = document.getString("description")

                // display organizer information


            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Error getting document reference: $e")
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDateForDisplay(date: String?): String {
        /**
         * Takes in a date in format yyyy-MM-dd (from Firestore) and returns a string of the date in the correct display format, with day of week.
         */
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy", Locale.ENGLISH)
        val localDate = LocalDate.parse(date)
        val formattedDate = localDate.format(formatter)

        Log.d(TAG, "formatted date: $formattedDate")
        return formattedDate.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatFirestoreTimeToDisplayString(timeMap: Map<String, Int?>): Any {
        /**
         * Takes in a "time" map (from Firestore) and returns a string of the time in the correct display format, with AM or PM.
         * For example, "12:00PM"
         */
        var hour = timeMap!!["hour"]!!
        var minute = timeMap!!["minute"]!!
        var meridian = "AM"
        if (hour >= 12) {
            meridian = "PM"
            if (hour != 12) {
                hour -= 12
            }
        }
        var displayTime = LocalTime.of(hour, minute)
        return "$displayTime$meridian"
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