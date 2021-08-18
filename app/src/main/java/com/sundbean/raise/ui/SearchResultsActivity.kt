package com.sundbean.raise.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sundbean.raise.Opportunity
import com.sundbean.raise.R
import com.sundbean.raise.adapters.MainFeedItemAdapter
import kotlinx.android.synthetic.main.activity_search_results.*
import java.util.ArrayList

class SearchResultsActivity : AppCompatActivity() {

    private lateinit var tvSearchResultsHeadline : TextView
    private lateinit var resultsRecyclerView : RecyclerView
    private lateinit var opportunityArrayList: ArrayList<Opportunity>
    private lateinit var resultsItemAdapter: MainFeedItemAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        tvSearchResultsHeadline = findViewById(R.id.tvSearchResultsTitle)
        resultsRecyclerView = findViewById(R.id.rvSearchResults)
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.setHasFixedSize(true)

        var searchQuery = intent.getStringExtra("search_query")

        opportunityArrayList = arrayListOf()

        resultsItemAdapter = MainFeedItemAdapter(opportunityArrayList, this)

        resultsRecyclerView.adapter = resultsItemAdapter

        // Retrieves search results to set up recyclerview and then calls setLocation() to update Headline with number of results
        eventChangeListener()

        btnBack.setOnClickListener {
            finish()
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
                    resultsItemAdapter.notifyDataSetChanged()
                }
            })

        var numResults = opportunityArrayList.size
        setLocation(numResults)
    }

    private fun setLocation(numResults : Int) {
        var currentUserUID = FirebaseAuth.getInstance().uid ?: ""
        val uid = currentUserUID as String
        val db = Firebase.firestore
        var userRef = db.collection("users").document(uid)
        userRef.get().addOnSuccessListener { userDoc ->
            var location = userDoc.get("location") as Map<String, *>
            var city = location.get("locality") as String

            tvSearchResultsHeadline.text = "Results in $city"
        }
    }
}