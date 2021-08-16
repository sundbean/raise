package com.sundbean.raise

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_event_details.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class EventDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var db : FirebaseFirestore
    private lateinit var mapView: MapView
    private lateinit var displayImageView : ImageView
    private lateinit var organizerRef : DocumentReference
    private lateinit var eventRef : DocumentReference
    private lateinit var eventDoc : DocumentSnapshot
    private lateinit var eventId : String
    private var userUid : String? = null
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

        db = Firebase.firestore
        displayImageView = findViewById(R.id.ivGroupDetailImage)
        mapView = findViewById(R.id.mapView)
        userUid = FirebaseAuth.getInstance().currentUser?.uid
        eventId = intent.getStringExtra("event_id") as String

        // retrieveEventData() -> prepareCoordinatesForMapDisplay(), fillViewsWithDataFromFirestore(), setClickListeners()
        retrieveEventData()

        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun retrieveEventData() {
        /**
         * Gets event document from firestore, then executes sub-methods to use the event's data to fill layout's views.
         * These sub-methods are nested in this method, because they depend on initialization of the lateinit variables [eventDoc]
         * and [eventRef]
         */
        eventRef = db.collection("events").document(eventId)
        eventRef.get()
            .addOnSuccessListener { document ->
                eventDoc = document
                // sub-methods
                prepareCoordinatesForMapDisplay()
                fillViewsWithDataFromFirestore()
                setClickListeners()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Error getting document reference: $e")
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prepareCoordinatesForMapDisplay() {
        /**
         * Populates latitude and longitude lateinit variables. These coordinates will be used
         */
        val location = eventDoc.get("location") as Map<*, *>
        val coordinates = location.get("coordinates") as Map<*, Double>
        latitude = coordinates["latitude"]
        longitude = coordinates["longitude"]
    }


    private fun setClickListeners() {
        ibBackButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnJoinGroup.setOnClickListener {
            if (btnJoinGroup.text == "RSVP") {
                eventRef.update("attendees", FieldValue.arrayUnion(userUid))
                btnJoinGroup.text = "cancel RSVP"
            } else {
                eventRef.update("attendees", FieldValue.arrayRemove(userUid))
                btnJoinGroup.text = "RSVP"
            }
        }

        llReturnToTop.setOnClickListener {
            svEventDetailsPage.scrollTo(0,0)
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillViewsWithDataFromFirestore() {
        displayImage()
        displayEventTitle()
        displayNumberOfAttendees()
        displayEventTime()
        displayEventDate()
        displayEventDescription()
        setRSVPButtonText()

        // sets off cascade of method calls to display organizer data
        retrieveOrganizerData()
    }

    private fun retrieveOrganizerData() {
        Log.d(TAG, "I'm in retrieveOrganizerData")
        val organizer = eventDoc.getString("organizer")!!
        if (eventDoc.getString("organizerType") == "user") {
            organizerRef = db.collection("users").document(organizer)
        } else if (eventDoc.getString("organizerType") == "group") {
            organizerRef = db.collection("groups").document(organizer)
        } else {
            Log.e(TAG, "Invalid organizerType: ${eventDoc.getString("organizerType")}")
        }

        organizerRef.get()
            .addOnSuccessListener { doc ->
                Log.d(TAG, "Organizer document successfully retrieved: $doc")
                displayOrganizerTitle(doc)
                displayOrganizerLocation(doc)
                displayOrganizerPhoto(doc)
                displayOrganizerAbout(doc)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error retrieving organizer document: $e")
            }
    }

    private fun displayOrganizerAbout(doc: DocumentSnapshot?) {
        if (doc != null) {
            tvEventDetailOrganizerAbout.text = doc.getString("about")
        }
    }

    private fun displayOrganizerPhoto(doc: DocumentSnapshot?) {
        val imgUrl = doc?.getString("photoUrl")
        Glide.with(this).load(imgUrl).into(ivEventDetailOrganizerImage)
    }

    private fun displayOrganizerLocation(doc: DocumentSnapshot?) {
        val location = doc?.get("location") as Map<*, *>
        val city = location.get("locality")
        val state = convertStateNameToAbbreviation(location.get("admin") as String?)
        tvEventDetailEventOrganizerCity.text = "$city, $state"
    }

    private fun displayOrganizerTitle(doc : DocumentSnapshot) {
        Log.d(TAG, "I'm in displayOrganizerTitle")
        tvEventDetailEventOrganizerName.text = doc.getString("name")
    }

    private fun setRSVPButtonText() {
        val attendees = eventDoc.get("attendees") as ArrayList<*>
        if (userUid in attendees) {
            btnJoinGroup.text = "cancel RSVP"
        } else {
            btnJoinGroup.text = "RSVP"
        }
    }

    private fun displayEventDescription() {
        tvGroupDetailAbout.text = eventDoc.getString("description")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayEventDate() {
        val date: String = formatDateForDisplay(eventDoc.getString("date"))
        tvEventDetailDate.text = date
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayEventTime() {
        val startTime =
            formatFirestoreTimeToDisplayString(eventDoc.get("startTime") as Map<String, Int?>)
        val endTime =
            formatFirestoreTimeToDisplayString(eventDoc.get("endTime") as Map<String, Int?>)
        tvEventDetailTime.text = "$startTime-$endTime"
    }

    private fun displayNumberOfAttendees() {
        val numAttendees = eventDoc.get("rsvpNum")
        tvNumberOfMembers.text = "$numAttendees going"
    }

    private fun displayEventTitle() {
        tvGroupDetailTitle.text = eventDoc.getString("name")
    }

    private fun displayImage() {
        val imgUrl = eventDoc.getString("photoUrl")
        Glide.with(this).load(imgUrl)
            .override(Resources.getSystem().getDisplayMetrics().widthPixels).into(displayImageView)
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
        val minute = timeMap!!["minute"]!!
        var meridian = "AM"
        if (hour >= 12) {
            meridian = "PM"
            if (hour != 12) {
                hour -= 12
            }
        }
        val displayTime = LocalTime.of(hour, minute)
        return "$displayTime$meridian"
    }

    /**
     * The below methods are required for Google map display in MapView
     */
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
        val coordinates = LatLng(latitude!!, longitude!!)
        map.addMarker(MarkerOptions().position(coordinates).title("Marker"))
        val zoomLevel = 16.0f // this goes up to 21
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