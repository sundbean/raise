package com.sundbean.raise.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sundbean.raise.Opportunity
import com.sundbean.raise.R
import com.sundbean.raise.adapters.EventFeedItemAdapter
import kotlinx.android.synthetic.main.fragment_explore.*


class ExploreFragment:Fragment(R.layout.fragment_explore) {

    private lateinit var exploreFeedHeadline: TextView
    private lateinit var yourEventsRecyclerView: RecyclerView
    private lateinit var yourEventsArrayList: ArrayList<Opportunity>
    private lateinit var userEvents: MutableList<DocumentSnapshot>
    private var db = Firebase.firestore
    private var currentUserUID = FirebaseAuth.getInstance().uid ?: ""
    private var currentUserRef = db.collection("users").document(currentUserUID)
    private var TAG = "ExploreFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        yourEventsRecyclerView = view.findViewById(R.id.rvRSVPEventsComingUp)
        exploreFeedHeadline = view.findViewById(R.id.tvWhatsInYourLocation)
        yourEventsArrayList = arrayListOf()

        currentUserRef.get().addOnSuccessListener { userDoc ->
            setLocation(userDoc)
            initYourEventsRecyclerView(userDoc)

        }

        initSearch()

    }

    private fun setLocation(userDoc: DocumentSnapshot) {
        val location = userDoc.get("location") as Map<String, *>
        val city = location["locality"] as String

        exploreFeedHeadline.text = "What's going on in $city?"
    }

    private fun initSearch() {
        /**
         * Starts a new activity containing search results when the user submits text in the search box
         */
        searchView.queryHint = "Enter Keyword"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //TODO: What do we do if the search term is invalid? How do we handle this?
            override fun onQueryTextSubmit(query: String?): Boolean {
                val intent = Intent(activity, SearchResultsActivity::class.java)
                intent.putExtra("search_query", query)
                startActivity(intent)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun initYourEventsRecyclerView(userDoc: DocumentSnapshot) {
        yourEventsRecyclerView.setHasFixedSize(true)

//         make it horizontal
        val horizontalLayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        yourEventsRecyclerView.layoutManager = horizontalLayoutManager

//        val userEventUids = userDoc.get("events") as ArrayList<String>
//        userEvents = mutableListOf()
//        Log.d(TAG, "userEventUids: $userEventUids")

        val eventsItemAdapter = EventFeedItemAdapter(yourEventsArrayList, requireActivity())
        yourEventsRecyclerView.adapter = eventsItemAdapter

        val userEvents = userDoc.get("events") as ArrayList<DocumentReference>
        //TODO: There has got to be a better way to query this without doing a read for every event! This is not scalable
        for (userEvent in userEvents) {
            userEvent.get()
                .addOnSuccessListener { doc ->
                    Log.d(
                        TAG,
                        "Successfully retrieved document: ${doc.id} named ${doc.getString("name")}"
                    )
                    val event = doc.toObject(Opportunity::class.java)
                    event?.setuid(event.id)
                    if (event != null) {
                        yourEventsArrayList.add(event)
                    }
                    eventsItemAdapter.notifyDataSetChanged()
                }
        }


        Log.d(TAG, "userEvents: $userEvents")
        Log.d(TAG, "yourEventsArrayList: $yourEventsArrayList")

    }
}