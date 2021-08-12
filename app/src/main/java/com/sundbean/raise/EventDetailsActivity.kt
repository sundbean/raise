package com.sundbean.raise

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
                val storage = FirebaseStorage.getInstance()
                val imgUrl = document.getString("photoUrl")
                Log.d(TAG, "Image url is $imgUrl")
                Glide.with(this).load(imgUrl).override(Resources.getSystem().getDisplayMetrics().widthPixels).into(displayImageView)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Error getting document reference: $e")
            }

    }

    //TODO: Programmatically make image height according to desired aspect ratio
    //TODO: Fix scroll issue like other scroll view layouts

}