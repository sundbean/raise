package com.sundbean.raise

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.activity_event_confirmation.*
import java.net.URL
import java.util.*
import kotlin.concurrent.timerTask

class EventConfirmationActivity : AppCompatActivity() {

    private var TAG = "EventConfirmationActivity"
    private var eventId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_confirmation)

        val intent = getIntent()
        eventId = intent.getStringExtra("event_id") as String

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
//                    tvEventLocation.text = document.getString("location")
                    // I've commented this part out because there's a timing issue here. This activity executes before Firebase has had a chance to
                    // store the image and assign an image url to the field in the new event's document. Either I'll havee to leave the image out, or
                    // find a way to "delay" the start of this activity for 1-2 seconds (to be coded in previous CreateEventActivity)
                    Handler(getMainLooper()).postDelayed({
                        var imageUrl = document.getString("photoUrl")
                        Log.d(TAG, "imageUrl is equal to: $imageUrl")
                        setImageInImageView(imageUrl)
                    }, 1000)
                }

                btnGoToEventDetail.setOnClickListener {
                    val intent =
                        Intent(this@EventConfirmationActivity, EventDetailsActivity::class.java)
                    intent.putExtra("event_id", eventId)
                    startActivity(intent)
                }

                btnGoToMainFeed.setOnClickListener {
                    val intent =
                        Intent(this@EventConfirmationActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                }
            }
        }

    private fun setImageInImageView(imageUrl : String?) {
        Log.d(TAG, "Image url is $imageUrl")
        Glide.with(this).load(imageUrl).override(Resources.getSystem().getDisplayMetrics().widthPixels).into(ivEventPhotoC)
    }
}