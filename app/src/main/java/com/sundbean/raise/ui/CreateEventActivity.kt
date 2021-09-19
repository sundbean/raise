package com.sundbean.raise.ui

import android.app.*
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.sundbean.raise.BuildConfig.MAPS_API_KEY
import com.sundbean.raise.R
import com.sundbean.raise.adapters.CausesGridItemAdapter
import com.sundbean.raise.models.Cause
import com.sundbean.raise.models.OnSelectedCauseClickListener
import com.sundbean.raise.models.OnUnselectedCauseClickListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.activity_event_details.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


class CreateEventActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var db : FirebaseFirestore
    private lateinit var autocompleteFragment : AutocompleteSupportFragment
    private var selectedPlaceId: String? = null
    private lateinit var selectedPlaceCoordinates: LatLng
    private lateinit var selectedCauses: MutableList<String>
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
    private lateinit var eventOrganizer : DocumentReference
    private lateinit var selectedDate : String
    private lateinit var userRef : DocumentReference
    private var groupsArray : MutableList<String>? = null
    private var selectedGroup : String? = null
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
        btnCreateEvent = findViewById(R.id.btnCreateEvent)
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
        url = ""
        selectedCauses = mutableListOf()
        db = Firebase.firestore
        val currentUserUID = FirebaseAuth.getInstance().uid ?: ""
        userRef = db.collection("users").document(currentUserUID)


        initPhotoSelection()
        initEventTypeSpinner()
        initDateAndTimeSelection()
        initAutoCompleteFragment()
        initCausesRecyclerView()
        setGroupOrganizerRadioButtonClickListener()
        setCreateEventClickListener()

    }


    ////////////////////////////////////////////////// IMAGE PICKER ////////////////////////////////////////////

    private fun initPhotoSelection() {
        /**
         * When the event photo container is clicked, the user wants to pick a photo
         */
        rlUploadImage.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        /**
         * Initiates image picker
         */
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /**
         * This gets called as a result of [pickImageFromGallery]
         */
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

    private fun launchImageCrop(uri: Uri) {
        /**
         * Gets called inside [onActivityResult] function.
         */
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1920, 1080)
            .setCropShape(CropImageView.CropShape.RECTANGLE) // this can be made oval
            .start(this)
    }

    private fun setImage(uri: Uri?) {
        /**
         * Gets called inside [onActivityResult] function.
         */
        // take down the upload icon
        llUploadPhotoCE.visibility = View.GONE
        // use glide to set the image
        Glide.with(this)
            .load(uri)
            .override(Resources.getSystem().getDisplayMetrics().widthPixels)
            .into(ivEventPhoto)
    }


    ////////////////////////////////////////////////// EVENT TYPE SPINNER ////////////////////////////////////////////

    private fun initEventTypeSpinner() {
        val eventTypeSpinner : Spinner = findViewById(R.id.spinnerEventType)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.event_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            eventTypeSpinner.adapter = adapter
        }

        setEventSelectionListener(eventTypeSpinner)
    }

    private fun setEventSelectionListener(eventTypeSpinner: Spinner) {
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
    }


    ////////////////////////////////////////////////// DATE AND TIME PICKERS ////////////////////////////////////////////

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initDateAndTimeSelection() {
        tvDate.setOnClickListener {
            pickDate()
        }

        tvStartTime.setOnClickListener {
            pickTime("start")
        }

        tvEndTime.setOnClickListener {
            pickTime("end")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pickDate() {
        /**
         * This is called from [initDateAndTimeSelection]
         */
        cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            val date = LocalDate.of(year, monthOfYear, dayOfMonth)
            // need to change this to black because its gray when Activity first loads
            tvDate.setTextColor(resources.getColor(R.color.black))
            tvDate.text = date.toString()
            selectedDate = date.toString()
        }, year, month, day).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pickTime(startOrEnd : String) {
        /**
         * This is called from [initDateAndTimeSelection]
         */
        val mcurrentTime = Calendar.getInstance()
        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)

        TimePickerDialog(this,
            { _, hourOfDay, minute ->
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


    ////////////////////////////////////////////////// AUTOCOMPLETE FRAGMENT ////////////////////////////////////////////

    private fun initAutoCompleteFragment() {
        // Initialize the AutocompleteSupportFragment (for user location)
        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.afCreateEvent)
                    as AutocompleteSupportFragment

        // Style the autocomplete fragment view
        val fView : View? = autocompleteFragment.view
        val etTextInput : EditText = fView!!.findViewById(R.id.places_autocomplete_search_input)
        etTextInput.textSize = 16.0f

        // make the autocomplete icon line up with the other icons
        autocompleteFragment.view?.setPadding(-40, 0, 0, 0)

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS))

        handlePlaceSelection()
    }

    private fun handlePlaceSelection() {
        /**
         * This gets called inside [initAutoCompleteFragment]
         */
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                selectedPlaceId = place.id
                selectedPlaceCoordinates = place.latLng!!
            }

            override fun onError(status: Status) {
                Log.i("Location Activity", "An error occurred: $status")
            }
        })

        Places.initialize(applicationContext, MAPS_API_KEY)
//        val placesClient = Places.createClient(this)
    }


    ////////////////////////////////////////////////// CAUSES RECYCLERVIEW ////////////////////////////////////////////

    private fun initCausesRecyclerView() {
        /**
         * Initializes the causes recycler view in a Grid layout. The click listeners passed to the recyclerview adapter
         * handle user selection of a cause card and user de-selection of a cause card, respectively. They are passed into
         * recyclerview in this way so that clicks can manipulate the [selectedCauses] array, which is initialized and handled
         * here in the activity.
         */

        val causesRecyclerView : RecyclerView = findViewById(R.id.rvChooseEventCauses)
        val causesArrayList : ArrayList<Cause> = arrayListOf()

        val causesItemAdapter = CausesGridItemAdapter(this, causesArrayList, object :
            OnUnselectedCauseClickListener {
            override fun onItemClick(cause: Cause?) {
                if (cause != null) {
                    cause.id?.let { selectedCauses.add(it) }
                    Log.d(TAG, "cause selected. selectedCauses: $selectedCauses")
                }
            }
        }, object : OnSelectedCauseClickListener {
            override fun onItemClick(cause: Cause?) {
                if (cause != null) {
                    cause.id?.let { selectedCauses.remove(it) }
                    Log.d(TAG, "cause unselected. selectedCauses: $selectedCauses")
                }
            }
        })

        // sets Recyclerview in a grid layout, 3 cards to a row
        GridLayoutManager(
            this.baseContext,
            3,
            RecyclerView.VERTICAL,
            false
        ).apply {
            causesRecyclerView.layoutManager = this
        }

        causesRecyclerView.adapter = causesItemAdapter

        // fixes uneven scrolling issue
        causesRecyclerView.setNestedScrollingEnabled(false)

        eventChangeListener(causesItemAdapter, causesArrayList)
    }

    private fun eventChangeListener(causesGridItemAdapter: CausesGridItemAdapter, causesArrayList : ArrayList<Cause>) {
        /**
         * Gets called inside [initCausesRecyclerView]. Notifies Recyclerview adapter in order to fill recyclerview
         * with cards that each correspond to a cause, for user selection.
         */
        val db = FirebaseFirestore.getInstance()
        db.collection("causes")
            .addSnapshotListener(object: com.google.firebase.firestore.EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    // loop through all the documents
                    for (dc : DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED){
                            val cause = dc.document.toObject(Cause::class.java)
                            cause.setUid(dc.document.id)
                            Log.d("Firestore debug", "cause object is : $cause")
                            causesArrayList.add(cause)
                        }
                    }
                    causesGridItemAdapter.notifyDataSetChanged()
                }
            })
    }


    ////////////////////////////////////////////////// ORGANIZER SELECTION (GROUP)  ////////////////////////////////////////////

    /**
     * The code in this section handles the user's selection of "A group I run" as the event's organizer.
     */

    private fun setGroupOrganizerRadioButtonClickListener() {
        getThisUsersModeratedGroupsFromFirestore()

        rbGroupOrganizer.setOnClickListener {
            askUserToSelectGroup()
        }
    }

    private fun getThisUsersModeratedGroupsFromFirestore() {
        /**
         * Prepares [groupsArray] for use in "Select Group" dialog, which will show if the user selects "A group I run" as the event's organizer.
         * This function grabs the authenticated user's document from Firestore, then fetches the array of document references under the field "groups".
         * Then, it fetches the group document for each group document reference, and adds the group's name to the [groupsArray] mutable list. This function
         * will get called whether or not the user selects 'A group I run', because it takes a second or two to fetch all the data from firestore, and it has
         * to be ready by the time the user selects 'A group I run' in order for that function to work.
         */
        groupsArray = mutableListOf()
        val userId = FirebaseAuth.getInstance().uid!!
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                Log.d(TAG, "User doc retrieved in createGroupsArrayList()")
                val groupsModerated = doc.get("groupsModerated") as ArrayList<DocumentReference>
                Log.d(TAG, "groupsModerated: $groupsModerated")
                for (group in groupsModerated) {
                    group.get()
                        .addOnSuccessListener {
                            Log.d(TAG, "Group doc retrieved in createGroupsArrayList(): ${it.getString("name")}")
                            // add the name of the group to the groupNames array list
                            val groupName = it.getString("name")
                            if (groupName != null) {
                                groupsArray?.add(groupName)
                            }
                            Log.d(TAG, "groupsArray: $groupsArray")
                        }

                }
            }
    }

    private fun askUserToSelectGroup() {
        /**
         * Initiates and then shows a dialog containing radio buttons that correspond to each group that the user moderates.
         */
        // setup the alert builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a group:")

        // add a radio button list
        val groups : Array<String>? = groupsArray?.toTypedArray()
        val checkedItem = 1

        builder.setSingleChoiceItems(groups, checkedItem,
            DialogInterface.OnClickListener { dialog, which ->
                selectedGroup = groups?.get(which)
            })

        // add OK and Cancel buttons
        builder.setPositiveButton("OK") { dialog, which ->
            // user clicked OK
        }
        builder.setNegativeButton("Cancel", null)

        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }


    ////////////////////////////////////////////////// CREATE THE EVENT  ////////////////////////////////////////////

    /**
     * All the code in this section handles event creation. When the user clicks the "Create Event" button, this sets off a cascade of function calls
     * that (1) process the user-entered data in the Create Event form (i.e. prepare it for the database) and (2) save the processed data to Firestore.
     * ------------------------------------------------------------------------------------------------------------------------------------------------
     * 1. [setCreateEventClickListener] sets the click listener on the "Create Event" button.
     * 2. Immediately upon click, [eventOrganizerType] and [eventOrganizer] are determined from the selected radio button.
     * 3. Then, [setCreateEventClickListener] calls [performEventCreation].
     * 4. [performEventCreation] first makes sure that all the required parts of the form have been filled out by the user.
     * 5. Then, [performEventCreation] calls [saveEventToFirestore].
     * 6. [saveEventToFirestore] gets the user-inputted data and uses it to create a hash map, which mirrors what this event's Firestore document will look
     *    like. Some of the values of this hash map ([eventType], [selectedDate], [eventStartTime], [eventEndTime], [eventOrganizerType], [eventOrganizer],
     *    and [userRef]) have already been processed by the time we get here. This function will call [getLocationDataFromCoordinates] and [getCausesRefs]
     *    in order to process the event's location and selected causes.
     * 7. Then, [saveEventToFirestore] calls [updateDatabaseWithEvent] and passes the [data] hashmap it just created, as well as the [causesRefs] it just retrieved.
     * 8. [updateDatabaseWithEvent] adds the [data] hashmap (in the form of a document) to the "events" collection in Firestore. Upon success, we get the document
     *    reference (stored in [eventReference]). We pass this document reference to a (a) series of functions that update other parts of Firestore with the new
     *    event, (b) saves the event's photo to Firebase storage, and (c) start the Event Confirmation activity.
     *      a. [addEventToUserEvents] adds the event's document reference to the user document's 'events' array in Firestore. [addEventToGroupsEvents] adds the event's
     *         document reference to the group organizer's 'events' array in Firestore, if applicable. [addEventToApplicableCauses] adds the event's document reference
     *         to each of the selected causes' 'events' array in Firestore.
     *      b. [uploadImageToFirebaseStorage] saves the selected image to Firebase storage, then gets the image's download URL. The function then updates the event
     *         document's "photoUrl" field with this url (when the event was first created, this was left blank).
     *      c. [startEventDetailsActivityWithEventId] creates a new intent, with the new event's id attached, and starts the Event Details activity to confirm event
     *         creation.
     */

    private fun setCreateEventClickListener() {
        /**
         * Sets the click listener for the "Create Event" button. If the user clicks this button, this triggers this function to check what
         * the user has selected has selected as the event's organizer ("Who is organizing this event?"). If it is the user ("I am"), then
         * [eventOrganizerType] is set to "user" and [eventOrganizer] is set to the user's document reference. If it is one of the user's moderated
         * groups ("A group I run"), then [eventOrganizerType] is set to "group" and [eventOrganizer] is set to the selected group's document reference.
         * After these two variables are set to their appropriate values, the [performEventCreation] function is called. This order is necessary, because
         * [performEventCreation] will insert the values of [eventOrganizerType] and [eventOrganizer] in the event document that is created in
         * [performEventCreation].
         */
        btnCreateEvent.setOnClickListener {
            val selectedOption: Int = rgOrganizer.checkedRadioButtonId
            rbOrganizer = findViewById(selectedOption)
            Log.d("CreateEventActivity", "Selected option is: ${rbOrganizer.text}")
            if (rbOrganizer.text == "I am") {
                eventOrganizerType = "user"
                eventOrganizer = userRef
                performEventCreation()
            } else if (rbOrganizer.text == "A group I run") {
                eventOrganizerType = "group"
                db.collection("groups").whereEqualTo("name", selectedGroup).get()
                    .addOnSuccessListener {
                        it.forEach { doc ->
                            eventOrganizer = doc.reference
                            Log.d(TAG, "Setting eventOrganizer variable: $eventOrganizer")
                            Log.d("CreateEventActivity", "I've made it to the performEventCreation() function")
                            // this is here to make sure it doesnt run until after the eventOrganizer variable has been assigned
                            performEventCreation()
                        }
                    }
            }
        }
    }


    private fun performEventCreation() {
        /**
         * Checks if any data is missing and, if so, prompts the user for the data via a Toast. If all required has been entered,
         * then the event creation process begins ([saveEventToFirestore])
         */
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
                ).show()
            }

            else -> {
                saveEventToFirestore()
            }

        }
    }

    private fun saveEventToFirestore() {
        /**
         * This gets called inside [performEventCreation].
         */
        val eventName: String = etEventName.text.toString().trim()
        val eventDescription: String = etDescription.text.toString().trim()

        val locationData = getLocationDataFromCoordinates(selectedPlaceCoordinates)

        val causesRefs = getCausesRefs()

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
            "createdBy" to userRef,
            "attendees" to listOf(userRef),
            "rsvpNum" to 1,
            "causes" to causesRefs
        )
        Log.i(TAG, "User reference is: $userRef and uid is ${FirebaseAuth.getInstance().uid}")

        updateDatabaseWithEvent(data, causesRefs)

    }

    private fun getLocationDataFromCoordinates(coordinates: LatLng): Any? {
        /**
         * This gets called in [saveEventToFirestore]
         */
        val geocoder = Geocoder(this@CreateEventActivity, Locale.US)
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

    private fun getCausesRefs(): ArrayList<DocumentReference> {
        /**
         * This gets called in [saveEventToFirestore]
         */
        val causesRefs : ArrayList<DocumentReference> = arrayListOf()

        for (causeId in selectedCauses) {
            causesRefs.add(db.collection("causes").document(causeId))
        }

        return causesRefs
    }

    private fun updateDatabaseWithEvent(
        data: HashMap<String, Any?>,
        causesRefs: ArrayList<DocumentReference>
    ) {
        /**
         * This gets called in [saveEventToFirestore]
         */
        db.collection("events").add(data)
            .addOnSuccessListener { eventReference ->
                Log.d("CreateEventActivity", "Document added to firestore: $eventReference")

                addEventToUserEvents(eventReference)
                addEventToGroupsEvents(eventReference)
                addEventToApplicableCauses(eventReference, causesRefs)
                uploadImageToFirebaseStorage(eventReference)
                startEventDetailsActivityWithEventId(eventReference)
            }
            .addOnFailureListener { e ->
                Log.w("CreateEventActivity", "Error adding document", e)
            }

    }

    private fun addEventToUserEvents(eventReference: DocumentReference) {
        /**
         * This gets called in [updateDatabaseWithEvent]
         */
        userRef.update("events", FieldValue.arrayUnion(eventReference))
    }

    private fun addEventToGroupsEvents(eventReference: DocumentReference?) {
        /**
         * This gets called in [updateDatabaseWithEvent]
         */
        if (eventOrganizerType == "group") {
            eventOrganizer.update("events", FieldValue.arrayUnion(eventReference))
        }
    }

    private fun addEventToApplicableCauses(
        eventReference: DocumentReference?,
        causesRefs: ArrayList<DocumentReference>
    ) {
        /**
         * This gets called in [updateDatabaseWithEvent]
         */
        for (cause in causesRefs) {
            cause.update("events", FieldValue.arrayUnion(eventReference))
        }
    }

    private fun uploadImageToFirebaseStorage(eventRef : DocumentReference) {
        /**
         * This gets called in [updateDatabaseWithEvent]. The [selectedPhotoUri] has already been processed [See "Image Picker" section].
         * It stores the file in Firebase storage, and then updates the newly-created event's "photoUrl" field with the photo's download URL
         * for easy retrieval later on.
         */
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

    private fun startEventDetailsActivityWithEventId(eventReference: DocumentReference?) {
        /**
         * This gets called in [updateDatabaseWithEvent]
         */
        val intent = Intent(this@CreateEventActivity, EventConfirmationActivity::class.java)
        intent.putExtra("event_id", eventReference?.id)
        startActivity(intent)
    }


    ////////////////////////////////////////////////////// MISC //////////////////////////////////////////////////

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}