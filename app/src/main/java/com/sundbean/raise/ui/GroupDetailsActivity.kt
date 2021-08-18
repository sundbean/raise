package com.sundbean.raise.ui

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sundbean.raise.Opportunity
import com.sundbean.raise.R
import com.sundbean.raise.adapters.EventFeedItemAdapter
import kotlinx.android.synthetic.main.activity_group_details.*

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var groupId : String
    private lateinit var groupImageView : ImageView
    private lateinit var eventsRecyclerView : RecyclerView
    private lateinit var groupDoc : DocumentSnapshot
    private lateinit var groupRef : DocumentReference
    private lateinit var feedItemAdapter : EventFeedItemAdapter
    private lateinit var db: FirebaseFirestore
    private var userUid : String? = null
    private lateinit var eventsArrayList : ArrayList<Opportunity>
    private var TAG = "GroupDetailsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        db = Firebase.firestore
        groupImageView = findViewById(R.id.ivGroupDetailImage)
        eventsArrayList = arrayListOf()
        eventsRecyclerView = findViewById(R.id.rvGroupDetailEvents)
        groupId = intent.getStringExtra("group_id") as String

        retrieveGroupData()
        initRecyclerView()

        eventChangeListener()
    }

    private fun retrieveGroupData() {
        /**
         * Gets group document from firestore, then executes sub-methods to use the group's data to fill layout's views.
         * These sub-methods are nested in this method because they depend on initialization of the lateinit variables [groupDoc]
         * and [groupRef]
         */
        groupRef = db.collection("groups").document(groupId)
        groupRef.get()
            .addOnSuccessListener { document ->
                groupDoc = document
                // sub-methods
                fillViewsWithDataFromFirestore()
                setClickListeners()
            }
    }

    private fun setClickListeners() {
        ibBackButton.setOnClickListener {
            finish()
        }

        btnJoinGroup.setOnClickListener {

        }
    }

    private fun fillViewsWithDataFromFirestore() {
        displayGroupImage()
        displayGroupTitle()
        displayNumberOfMembers()
        displayGroupDescription()
        setJoinGroupButtonText()
    }

    private fun setJoinGroupButtonText() {
        val members = groupDoc.get("members") as ArrayList<*>
        if (userUid in members) {
            btnJoinGroup.text = "Leave Group"
        } else {
            btnJoinGroup.text = "Join Group"
        }
    }

    private fun displayGroupDescription() {
        val description = groupDoc.getString("description")
        tvGroupDetailAbout.text = description
    }

    private fun displayNumberOfMembers() {
        val numMembers = groupDoc.get("membersNum")
        tvNumberOfMembers.text = "$numMembers members"
    }

    private fun displayGroupTitle() {
        tvGroupDetailTitle.text = groupDoc.getString("name")
    }

    private fun displayGroupImage() {
        val imgUrl = groupDoc.getString("photoUrl")
        Glide.with(this).load(imgUrl)
            .override(Resources.getSystem().getDisplayMetrics().widthPixels).into(groupImageView)
    }

    private fun initRecyclerView() {
        eventsRecyclerView.setHasFixedSize(true)

//         make it horizontal
        val horizontalLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        eventsRecyclerView.layoutManager = horizontalLayoutManager

        feedItemAdapter = EventFeedItemAdapter(eventsArrayList, this)

        eventsRecyclerView.adapter = feedItemAdapter

    }

    private fun eventChangeListener() {
        db = FirebaseFirestore.getInstance()
        //TODO: narrow this down to only events that belong to the group - right now its just all events
        db.collection("events")
            .addSnapshotListener(object: com.google.firebase.firestore.EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e(TAG, error.message.toString())
                        return
                    }
                    // loop through all the documents
                    for (dc : DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED){
                            var event = dc.document.toObject(Opportunity::class.java)
                            event.setuid(dc.document.id)
                            Log.d(TAG, "opportunity object is : $event")
                            eventsArrayList.add(event)
                        }
                    }
                    feedItemAdapter.notifyDataSetChanged()
                }
            })
    }
}