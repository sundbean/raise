package com.sundbean.raise

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_create_event.*
import java.util.*

class CreateEventActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var causesRecyclerView : RecyclerView
    private var eventType : String? = null
    private lateinit var etEventName : EditText
    private lateinit var etDate : EditText
    private lateinit var etTime : EditText
    private lateinit var etLocation: EditText
    private lateinit var etDescription : EditText
    private lateinit var rgOrganizer : RadioGroup
    private lateinit var rbOrganizer : RadioButton
    private lateinit var btnCreateEvent : Button
    private lateinit var eventOrganizerType : String
    private lateinit var eventOrganizer : String
    private var selectedPhotoUri: Uri? = null
    private var eventPhotoUrl: String? = null
    private val GALLERY_REQUEST_CODE = 1234
    private val TAG = "CreateEventActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        rgOrganizer = findViewById(R.id.rgOrganizer)
        btnCreateEvent = findViewById(R.id.btnCreateEvent)
        etEventName = findViewById(R.id.etEventName)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        etLocation = findViewById(R.id.etLocation)
        etDescription = findViewById(R.id.etDescription)
        rgOrganizer = findViewById(R.id.rgOrganizer)

        // get device screen dimensions and use them to set the event image to 16/9 aspect ratio
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        var screenWidth = displayMetrics.widthPixels
        ivEventPhoto.getLayoutParams().height = (16/9) * screenWidth

        // when the event photo container is clicked, the user wants to pick a photo
        rlUploadImage.setOnClickListener {
            pickImageFromGallery()
        }


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
                uploadImageToFirebaseStorage()
                performEventCreation()
                val intent = Intent(this@CreateEventActivity, EventConfirmationActivity::class.java)
                startActivity(intent)
            }
        }

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
                        ivUploadIcon.setImageResource(0)
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
        // use glide to set the image
        Glide.with(this)
            .load(uri)
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

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) {
            //TODO: make profile picture optional
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File location: $it")
                    eventPhotoUrl = it.toString()
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
            TextUtils.isEmpty(etDate.text.toString().trim()) -> {
                Toast.makeText(
                    this@CreateEventActivity,
                    "Please add an event date.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            TextUtils.isEmpty(etTime.text.toString().trim()) -> {
                Toast.makeText(
                    this@CreateEventActivity,
                    "Please add an event time.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            TextUtils.isEmpty(etLocation.text.toString().trim()) -> {
                Toast.makeText(
                    this@CreateEventActivity,
                    "Please add an event location.",
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
                val eventDate: String = etDate.text.toString().trim()
                val eventTime: String = etTime.text.toString().trim()
                val eventLocation: String = etLocation.text.toString().trim()
                val eventDescription: String = etDescription.text.toString().trim()

                // save to Firestore
                val db = Firebase.firestore
                val data = hashMapOf(
                    "name" to eventName,
                    "photo" to eventPhotoUrl,
                    "type" to eventType,
                    "date" to eventDate,
                    "time" to eventTime,
                    "location" to eventLocation,
                    "organizerType" to eventOrganizerType,
                    "organizer" to eventOrganizer,
                    "description" to eventDescription,
                    "createdBy" to FirebaseAuth.getInstance().uid
                )
                db.collection("events").document().set(data)
                    .addOnSuccessListener {
                        Log.d("CreateEventActivity", "Document added to firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.w("CreateEventActivity", "Error adding document", e)
                    }

            }

        }
    }

}