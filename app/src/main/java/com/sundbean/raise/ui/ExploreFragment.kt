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
import com.sundbean.raise.adapters.GroupFeedItemAdapter
import com.sundbean.raise.models.Group
import kotlinx.android.synthetic.main.fragment_explore.*


class ExploreFragment:Fragment(R.layout.fragment_explore) {

    private lateinit var exploreFeedHeadline: TextView
    private lateinit var yourEventsRecyclerView: RecyclerView
    private lateinit var yourEventsArrayList: ArrayList<Opportunity>
    private lateinit var popularGroupsRecyclerView: RecyclerView
    private lateinit var popularGroupsArrayList: ArrayList<Group>
    private var db = Firebase.firestore
    private var currentUserUID = FirebaseAuth.getInstance().uid ?: ""
    private var currentUserRef = db.collection("users").document(currentUserUID)
    //TODO: 'city' needs to reflect hte user's city. Two solutions: an intent extra, or figure out how to call initPopularGroupsRecyclerView only after city has been retrieved from user doc in firestore. Research coroutines
    private var city = "Seattle"
    private var TAG = "ExploreFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        yourEventsRecyclerView = view.findViewById(R.id.rvRSVPEventsComingUp)
        yourEventsArrayList = arrayListOf()
        popularGroupsRecyclerView = view.findViewById(R.id.rvPopularGroupsInYourLocation)
        popularGroupsArrayList = arrayListOf()
        exploreFeedHeadline = view.findViewById(R.id.tvWhatsInYourLocation)

        currentUserRef.get().addOnSuccessListener { userDoc ->
            val location = userDoc.get("location") as Map<String, *>
            city = location["locality"] as String
            exploreFeedHeadline.text = "What's going on in $city?"

            initYourEventsRecyclerView(userDoc)
        }

        initSearch()
        initPopularGroupsRecyclerView()

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

        val eventsItemAdapter = EventFeedItemAdapter(yourEventsArrayList, requireActivity())
        yourEventsRecyclerView.adapter = eventsItemAdapter

        val userEvents = userDoc.get("events") as ArrayList<DocumentReference>
        for (userEvent in userEvents) {
            userEvent.get()
                .addOnSuccessListener { doc ->
                    Log.d(
                        TAG,
                        "Successfully retrieved document: ${doc.id} named ${doc.getString("name")}"
                    )
                    val event = doc.toObject(Opportunity::class.java)
                    event?.setuid(doc.id)
                    if (event != null) {
                        yourEventsArrayList.add(event)
                    }
                    eventsItemAdapter.notifyDataSetChanged()
                }
        }
    }

    private fun initPopularGroupsRecyclerView() {
        /**
         * Right now, this just puts all groups in the recyclerview for presentation purposes.
         * Future intention: Query the database for the most popular groups in the user's selected location and display those.
         */
        popularGroupsRecyclerView.setHasFixedSize(true)

//         make it horizontal
        val horizontalLayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        popularGroupsRecyclerView.layoutManager = horizontalLayoutManager

        val groupsItemAdapter = GroupFeedItemAdapter(popularGroupsArrayList, requireActivity())
        popularGroupsRecyclerView.adapter = groupsItemAdapter

        eventChangeListener(groupsItemAdapter)
    }

    private fun eventChangeListener(groupsItemAdapter: GroupFeedItemAdapter) {
        db.collection("groups").whereEqualTo("city", city)
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
                            val group = dc.document.toObject(Group::class.java)
                            group.setuid(dc.document.id)
                            Log.d("Firestore debug", "group object is : $group")
                            popularGroupsArrayList.add(group)
                        }
                    }
                    groupsItemAdapter.notifyDataSetChanged()
                }
            })
    }
}