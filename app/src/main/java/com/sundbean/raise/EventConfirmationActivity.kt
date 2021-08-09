package com.sundbean.raise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_event_confirmation.*
import java.net.URL

class EventConfirmationActivity : AppCompatActivity() {

    private var TAG = "EventConfirmationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_confirmation)

        val intent = getIntent()
        val eventId = intent.getStringExtra("event_id") as String

        displayEventInfo(eventId)


    }

    private fun displayEventInfo(eventId : String) {
        val db = Firebase.firestore

        val eventRef = db.collection("events").document(eventId)
        eventRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "Document data: ${document.data}")
                    tvEventType.text = document.getString("type")
                    tvEventName.text = document.getString("name")
                    // get the user
                    val userId = document.getString("organizer") as String
                    val userRef = db.collection("users").document(userId)
                    userRef.get().addOnSuccessListener { userDoc ->
                        tvEventOrganizer.text = userDoc.getString("name")
                    }
                    tvEventDate.text = document.getString("date")
                    tvEventTime.text = document.getString("time")
                    tvEventDescription.text = document.getString("description")
                    // I've commented this part out because there's a timing issue here. This activity executes before Firebase has had a chance to
                    // store the image and assign an image url to the field in the new event's document. Either I'll havee to leave the image out, or
                    // find a way to "delay" the start of this activity for 1-2 seconds (to be coded in previous CreateEventActivity).
//                    downloadImageFromStorageThenSetItInImageView(document.getString("photoUrl")!!)
                }
            }
    }

//    private fun downloadImageFromStorageThenSetItInImageView(imageUrl : String) {
//        val storage = FirebaseStorage.getInstance()
//        Log.d(TAG, "Image url is $imageUrl")
//        val gsReference = storage.getReferenceFromUrl(imageUrl)
//        Glide.with(this).load(gsReference).into(ivEventPhotoC)
//    }
}