package com.sundbean.raise

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.sundbean.raise.BuildConfig.MAPS_API_KEY
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.activity_create_event.ivEventPhoto
import kotlinx.android.synthetic.main.activity_event_details.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class CreateEventActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var causesRecyclerView : RecyclerView
    private lateinit var autocompleteFragment : AutocompleteSupportFragment
    private var selectedPlaceId: String? = null
    private lateinit var selectedPlaceCoordinates: LatLng
    private var eventType : String? = null
    private lateinit var etEventName : EditText
    private lateinit var tvDate : TextView
    private lateinit var flDate : FrameLayout
    private lateinit var etDate : EditText
    private lateinit var flStartTime : FrameLayout
    private lateinit var tvStartTime : TextView
    private lateinit var etStartTime : EditText
    private lateinit var flEndTime : FrameLayout
    private lateinit var tvEndTime : TextView
    private lateinit var etEndTime : EditText
    private lateinit var eventStartTime : LocalTime
    private lateinit var eventEndTime : LocalTime
    private lateinit var etDescription : EditText
    private lateinit var rgOrganizer : RadioGroup
    private lateinit var rbOrganizer : RadioButton
    private lateinit var btnCreateEvent : Button
    private lateinit var eventOrganizerType : String
    private lateinit var tvAutocompleteHint : TextView
    private lateinit var eventOrganizer : String
    private lateinit var selectedDate : String
    private var selectedPhotoUri: Uri? = null
    private lateinit var url: String
    private val GALLERY_REQUEST_CODE = 1234
    private val TAG = "CreateEventActivity"
    private var cal = Calendar.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        rgOrganizer = findViewById(R.id.rgOrganizer)
        btnCreateEvent = findViewById(R.id.btnRSVP)
        etEventName = findViewById(R.id.etEventName)
        tvDate = findViewById(R.id.tvDate)
        flDate = findViewById(R.id.flEventDate)
        etDate = findViewById(R.id.etDate)
        etStartTime = findViewById(R.id.etStartTime)
        flStartTime = findViewById(R.id.flEventStartTime)
        tvStartTime = findViewById(R.id.tvStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        tvEndTime = findViewById(R.id.tvEndTime)
        flEndTime = findViewById(R.id.flEventEndTime)
        etDescription = findViewById(R.id.etDescription)
        rgOrganizer = findViewById(R.id.rgOrganizer)
        tvAutocompleteHint = findViewById(R.id.tvAutocompleteHint)
        url = ""

        // when the event photo container is clicked, the user wants to pick a photo
        rlUploadImage.setOnClickListener {
            pickImageFromGallery()
        }

        // Handling event type input
        val eventTypeSpinner : Spinner = findViewById(R.id.spinnerEventType)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.event_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            eventTypeSpinner.adapter = adapter
        }


        tvDate.setOnClickListener {
            pickDate()
        }

        tvStartTime.setOnClickListener {
            pickTime("start")
        }

        tvEndTime.setOnClickListener {
            pickTime("end")
        }

        // Make the causes recycler view
        causesRecyclerView = findViewById(R.id.rvChooseEventCauses)
        val causes = mutableListOf("img_animal_rights_foreground", "img_arts_and_culture_foreground", "img_black_lives_matter_foreground", "img_climate_change_foreground", "img_conservation_foreground", "img_education_foreground", "img_food_access_foreground", "img_homelessness_poverty_foreground", "img_lgbt_rights_foreground", "img_mental_health_foreground", "img_political_reform_foreground", "img_prison_reform_foreground", "img_refugee_rights_foreground", "img_water_sanitation_foreground", "img_womens_rights_foreground")
        GridLayoutManager(
            this.baseContext,
            3,
            RecyclerView.VERTICAL,
            false
        ).apply {
            causesRecyclerView.layoutManager = this
        }

        causesRecyclerView.adapter = RecyclerViewAdapter(this, causes)

        // fixes uneven scrolling issue
        causesRecyclerView.setNestedScrollingEnabled(false)

        eventTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // retrieve the selected item with parent.getItemAtPosition(pos)
                eventType = parent.getItemAtPosition(pos) as String
                Log.i("CreateEventActivity", "selected item: $eventType")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        // Initialize the AutocompleteSupportFragment (for user location)
        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.afCreateEvent)
                    as AutocompleteSupportFragment

        // Style the autocomplete fragment view
        var fView : View? = autocompleteFragment.view
        var etTextInput : EditText = fView!!.findViewById(R.id.places_autocomplete_search_input)
        etTextInput.setTextSize(16.0f)

        // make the autocomplete icon line up with the other icons
        autocompleteFragment.view?.setPadding(-40, 0, 0, 0)

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                // using the place id: https://developers.google.com/maps/documentation/places/web-service/place-id
                selectedPlaceId = place.id
                selectedPlaceCoordinates = place.latLng!!
                tvAutocompleteHint.setVisibility(View.GONE)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("Location Activity", "An error occurred: $status")
            }
        })

        //TODO: find a way to hide this api key!
        Places.initialize(applicationContext, MAPS_API_KEY)
        val placesClient = Places.createClient(this)

        btnCreateEvent.setOnClickListener {
            val selectedOption: Int = rgOrganizer!!.checkedRadioButtonId
            if (selectedOption == null) {
                Toast.makeText(this@CreateEventActivity, "Please select an event type.", Toast.LENGTH_SHORT).show()
            } else {
                rbOrganizer = findViewById(selectedOption)
                Log.d("CreateEventActivity", "Selected option is: ${rbOrganizer.text}")
                if (rbOrganizer.text == "I am") {
                    eventOrganizerType = "user"
                    eventOrganizer = FirebaseAuth.getInstance().uid ?: ""
                } else if (rbOrganizer.text == "A group I run") {
                    eventOrganizerType = "group"
                    eventOrganizer = "SELECTED_GROUP"
                }

                Log.d("CreateEventActivity", "I've made it to the performEventCreation() function")
                performEventCreation()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pickTime(startOrEnd : String) {
        val mcurrentTime = Calendar.getInstance()
        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)

        TimePickerDialog(this,
            { view, hourOfDay, minute ->
                val time = LocalTime.of(hourOfDay, minute)
                if (startOrEnd == "start") {
                    eventStartTime = time
                    tvStartTime.text = time.toString()
                    tvStartTime.setTextColor(Color.parseColor("#000000"))
                } else if (startOrEnd == "end") {
                    eventEndTime = time
                    tvEndTime.text = time.toString()
                    tvEndTime.setTextColor(Color.parseColor("#000000"))
                }
            }, hour, minute, false).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pickDate() {
        cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { view, year, monthOfYear, dayOfMonth ->
            val date = LocalDate.of(year, monthOfYear, dayOfMonth)
            // need to change this to black because its gray when Activity first loads
            tvDate!!.setTextColor(resources.getColor(R.color.black))
            tvDate!!.text = date.toString()
            selectedDate = date.toString()
        }, year, month, day).show()
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // retrieve data from intent
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                } else {
                    Log.e(TAG, "Image selection error: Couldn't select that image from memory")
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        selectedPhotoUri = it
                        setImage(it)
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(TAG, "Crop error: ${result.error}")
                }
            }
        }
    }

    private fun setImage(uri: Uri?) {
        // take down the upload icon
        ivUploadIcon.setImageResource(0)
        // use glide to set the image
        Glide.with(this)
            .load(uri)
            .override(Resources.getSystem().getDisplayMetrics().widthPixels)
            .into(ivEventPhoto)
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1920, 1080)
            .setCropShape(CropImageView.CropShape.RECTANGLE) // this can be made oval
            .start(this)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun uploadImageToFirebaseStorage(eventRef : DocumentReference) {
        Log.d(TAG, "I'm inside uploadImageToFirebaseStorage")

        if (selectedPhotoUri == null) {
            Log.d(TAG, "selectedPhotoUri was null so not uploaded to storage")
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File location: $it")
                    // this is here because there was no way to assign it.toString() to a variable that would work
                    // update photoUrl in event
                    eventRef.update("photoUrl", it.toString())
                }
            }
            .addOnFailureListener {
                Log.e(TAG,"There was an error adding image to firebase storage: $it")
            }
    }

    private fun performEventCreation() {
        when {
            TextUtils.isEmpty(etEventName.text.toString().trim()) -> {
                Toast.makeText(
                    this@CreateEventActivity,
                    "Please enter the name of this event.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            TextUtils.isEmpty(tvDate.text.toString().trim()) -> {
                Toast.makeText(
                    this@CreateEventActivity,
                    "Please add an event date.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            TextUtils.isEmpty(tvStartTime.text.toString().trim()) -> {
                Toast.makeText(
                    this@CreateEventActivity,
                    "Please add an event start time.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            TextUtils.isEmpty(tvEndTime.text.toString().trim()) -> {
                Toast.makeText(
                    this@CreateEventActivity,
                    "Please add an event end time.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            TextUtils.isEmpty(etDescription.text.toString().trim()) -> {
                Toast.makeText(
                    this@CreateEventActivity,
                    "Please add an event description.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            eventType == null -> {
                Toast.makeText(
                    this@CreateEventActivity,
                    "Please select an event type.",
                    Toast.LENGTH_SHORT
                )
            }

            else -> {
                Log.d("CreateEventActivity", "I've made it into performEventCreation() and am about to save to Firestore")
                val eventName: String = etEventName.text.toString().trim()
                val eventDescription: String = etDescription.text.toString().trim()

                var currentUserUID = FirebaseAuth.getInstance().uid ?: ""
                val uid = currentUserUID as String
                val db = Firebase.firestore
                var userRef = db.collection("users").document(uid)

                var locationData = getLocationDataFromCoordinates(selectedPlaceCoordinates)

                // save event to Firestore events collection
                val data = hashMapOf(
                    "name" to eventName,
                    "photoUrl" to "",
                    "oppType" to "event",
                    "type" to eventType,
                    "date" to selectedDate,
                    "startTime" to eventStartTime,
                    "endTime" to eventEndTime,
                    "location" to locationData,
                    "organizerType" to eventOrganizerType,
                    "organizer" to eventOrganizer,
                    "description" to eventDescription,
                    "createdBy" to FirebaseAuth.getInstance().uid,
                    "attendees" to listOf(userRef.id),
                    "rsvpNum" to 1
                )
                Log.i(TAG, "User reference is: $userRef and uid is ${FirebaseAuth.getInstance().uid}")
                db.collection("events").add(data)
                    .addOnSuccessListener { documentReference ->
                        Log.d("CreateEventActivity", "Document added to firestore: $documentReference")
                        // add the event to the user's events:
                        userRef.update("events", FieldValue.arrayUnion(documentReference.id))
                        uploadImageToFirebaseStorage(documentReference)
                        val intent = Intent(this@CreateEventActivity, EventConfirmationActivity::class.java)
                        intent.putExtra("event_id", documentReference.id)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Log.w("CreateEventActivity", "Error adding document", e)
                    }


            }

        }
    }

    private fun getLocationDataFromCoordinates(coordinates: LatLng): Any? {
        var geocoder = Geocoder(this@CreateEventActivity, Locale.US)
        val addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
        return hashMapOf (
            "address" to addresses[0].getAddressLine(0),
            "coordinates" to coordinates,
            "placeId" to selectedPlaceId,
            "locality" to addresses[0].locality,
            "admin" to addresses[0].adminArea,
            "subAdmin" to addresses[0].subAdminArea,
            "postalCode" to addresses[0].postalCode)
    }
}