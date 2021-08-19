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
import com.sundbean.raise.adapters.CausesFeedItemAdapter
import com.sundbean.raise.adapters.CausesGridItemAdapter
import com.sundbean.raise.adapters.EventFeedItemAdapter
import com.sundbean.raise.adapters.GroupFeedItemAdapter
import com.sundbean.raise.models.Cause
import com.sundbean.raise.models.Group
import kotlinx.android.synthetic.main.fragment_explore.*
import java.text.SimpleDateFormat
import java.util.*


class ExploreFragment:Fragment(R.layout.fragment_explore) {

    private lateinit var exploreFeedHeadline: TextView
    private lateinit var yourEventsRecyclerView: RecyclerView
    private lateinit var yourEventsArrayList: ArrayList<Opportunity>
    private lateinit var popularGroupsRecyclerView: RecyclerView
    private lateinit var popularGroupsArrayList: ArrayList<Group>
    private lateinit var upcomingEventsRecyclerView: RecyclerView
    private lateinit var upcomingEventsArrayList: ArrayList<Opportunity>
    private lateinit var causesRecyclerView: RecyclerView
    private lateinit var causesArrayList: ArrayList<Cause>
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
        upcomingEventsRecyclerView = view.findViewById(R.id.rvUpcomingEvents)
        upcomingEventsArrayList = arrayListOf()
        causesRecyclerView = view.findViewById(R.id.rvCausesHorizontal)
        causesArrayList = arrayListOf()
        exploreFeedHeadline = view.findViewById(R.id.tvWhatsInYourLocation)

        currentUserRef.get().addOnSuccessListener { userDoc ->
            val location = userDoc.get("location") as Map<String, *>
            city = location["locality"] as String
            exploreFeedHeadline.text = "What's going on in $city?"

            initYourEventsRecyclerView(userDoc)
        }

        initSearch()
        initPopularGroupsRecyclerView()
        initUpcomingEventsRecyclerView()
        initCausesRecyclerView()

    }

    private fun initCausesRecyclerView() {
        causesRecyclerView.setHasFixedSize(true)

        val horizontalLayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        causesRecyclerView.layoutManager = horizontalLayoutManager

        val causesItemAdapter = CausesFeedItemAdapter(requireActivity(), causesArrayList)
        causesRecyclerView.adapter = causesItemAdapter

        causesEventChangeListener(causesItemAdapter)
    }

    private fun causesEventChangeListener(adapter: CausesFeedItemAdapter) {
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
                    adapter.notifyDataSetChanged()
                }
            })
    }


    private fun initUpcomingEventsRecyclerView() {
        /**
         * Initializes recyclerview containing upcoming events in the selected location.
         */
        upcomingEventsRecyclerView.setHasFixedSize(true)

//         make it horizontal
        val horizontalLayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        upcomingEventsRecyclerView.layoutManager = horizontalLayoutManager

        val upcomingEventsItemAdapter = EventFeedItemAdapter(upcomingEventsArrayList, requireActivity())
        upcomingEventsRecyclerView.adapter = upcomingEventsItemAdapter

        upcomingEventsEventChangeListener(upcomingEventsItemAdapter)
    }

    private fun upcomingEventsEventChangeListener(adapter: EventFeedItemAdapter) {
        /**
         * Queries the database for all events that are occurring in the selected city, in the next week. Notifies the adapter
         * so that it can take the resulting list of events and display them in the recyclerview.
         */
        Log.d(TAG, "I'm in upcomingEventsEventChangeListener. Variable [city] is $city")
        val dateRange = getCurrentDateAndNextWeeksDate()
        db.collection("events").whereEqualTo("location.locality", city).whereGreaterThanOrEqualTo("date", dateRange[0]).whereLessThanOrEqualTo("date", dateRange[1])
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
                            val event = dc.document.toObject(Opportunity::class.java)
                            event.setuid(dc.document.id)
                            Log.d("Firestore debug", "upcoming event object is : $event")
                            upcomingEventsArrayList.add(event)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            })
    }

    private fun getCurrentDateAndNextWeeksDate(): Array<String> {
        /**
         * Gets the current date and calculates the date 7 days from now. Formats the dates into strings with format "year-month-day",
         * which is how dates are stored in the database. Returns an array containing two values: the first, today's date string, and
         * the second, next week's (7 days from now) date string. These strings will be used to query the database for all events
         * that are occurring in the next week.
         */
        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val nextWeeksDate = calendar.time

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return arrayOf(sdf.format(currentDate) as String, sdf.format(nextWeeksDate) as String)
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
        /**
         * Populates recyclerview with all events that user is attending.
         */
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
         * Right now, this lists all groups in the user's location for presentation purposes. When there's more in the database,
         * will need to find a way to sort the query results by member number.
         */
        popularGroupsRecyclerView.setHasFixedSize(true)

//         make it horizontal
        val horizontalLayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        popularGroupsRecyclerView.layoutManager = horizontalLayoutManager

        val groupsItemAdapter = GroupFeedItemAdapter(popularGroupsArrayList, requireActivity())
        popularGroupsRecyclerView.adapter = groupsItemAdapter

        popularGroupsEventChangeListener(groupsItemAdapter)
    }

    private fun popularGroupsEventChangeListener(groupsItemAdapter: GroupFeedItemAdapter) {
        /**
         * Queries the database for all groups in the selected city.
         */
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