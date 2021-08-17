package com.sundbean.raise

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_group_details.*
import java.security.AccessController.getContext

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var eventsRecyclerView : RecyclerView
    private lateinit var feedItemAdapter : MainFeedItemAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var eventsArrayList : ArrayList<Opportunity>
    private var TAG = "GroupDetailsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        eventsArrayList = arrayListOf()
        eventsRecyclerView = findViewById(R.id.rvGroupDetailEvents)

        initRecyclerView()

        eventChangeListener()



    }

    private fun initRecyclerView() {


        eventsRecyclerView.setHasFixedSize(true)

//         make it horizontal
        val horizontalLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        eventsRecyclerView.layoutManager = horizontalLayoutManager

        feedItemAdapter = MainFeedItemAdapter(eventsArrayList, this)

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