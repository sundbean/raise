package com.sundbean.raise.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sundbean.raise.*
import com.sundbean.raise.R
import com.sundbean.raise.adapters.MainFeedItemAdapter
import java.util.*


class MainFeedFragment:Fragment(R.layout.fragment_main_feed2) {

    private lateinit var tvMainFeedHeadline : TextView
    private lateinit var recyclerView : RecyclerView
    private lateinit var opportunityArrayList: ArrayList<Opportunity>
    private lateinit var feedItemAdapter: MainFeedItemAdapter
    private lateinit var db: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        tvMainFeedHeadline = view.findViewById(R.id.tvMainFeedHeadline)
        recyclerView = view.findViewById(R.id.rvMainFeed)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        setLocation()

        opportunityArrayList = arrayListOf()

        feedItemAdapter = MainFeedItemAdapter(opportunityArrayList, requireActivity())

        recyclerView.adapter = feedItemAdapter

        eventChangeListener()

        // An approach for dealing with dates:
        // Grab the year, month, day, hour, and minute from the date picker and time picker and store each in its own variable
        // Pass these variables to a "time conversion" class (that I need to make), which will:
        // // - if the functionality exists, create a timestamp directly from these variables
        // // - as a last resort, simply join them together in a string like "2021-10-12-16-45" and store the string as the "dateAndTime"
        // // // - if I do this, I can then store the "duration" as another field (value in minutes)
        // // // - in this format it will be easier to figure out dates in a certain provided time range
        // // // - then when i need to display hte dates, I can parse out the data from the string (possibly splitting at "-" and putting the values in an array) to display in my desired format

}

    private fun setLocation() {
        var currentUserUID = FirebaseAuth.getInstance().uid ?: ""
        val uid = currentUserUID as String
        val db = Firebase.firestore
        var userRef = db.collection("users").document(uid)
        userRef.get().addOnSuccessListener { userDoc ->
            var location = userDoc.get("location") as Map<String, *>
            var city = location.get("locality") as String

            tvMainFeedHeadline.text = "Events for you in $city"
        }
    }

    private fun eventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("events")
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
                            var opportunity = dc.document.toObject(Opportunity::class.java)
                            opportunity.setuid(dc.document.id)
                            Log.d("Firestore debug", "opportunity object is : $opportunity")
                            opportunityArrayList.add(opportunity)
                        }
                    }
                    feedItemAdapter.notifyDataSetChanged()
                }
            })
    }
}

