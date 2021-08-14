package com.sundbean.raise

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_event_details.*

class EventDetailsActivity : AppCompatActivity() {

    private lateinit var displayImageView : ImageView
    private var TAG = "EventDetailsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        displayImageView = findViewById(R.id.ivEventDetailsImage)

        val intent = getIntent()
        val eventId = intent.getStringExtra("event_id") as String

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

        ibBackButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            //get rid of any Login or Register Activities running in the background
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }

    }



    //TODO: Fix scroll issue like other scroll view layouts

}

data class Attendees(
    var attendeesList: ArrayList<String>
)