package com.sundbean.raise.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.sundbean.raise.R
import com.sundbean.raise.adapters.CausesGridItemAdapter
import com.sundbean.raise.models.Cause
import com.sundbean.raise.models.OnSelectedCauseClickListener
import com.sundbean.raise.models.OnUnselectedCauseClickListener

class CreateFundraiserActivity : AppCompatActivity() {

    private lateinit var causesRecyclerView : RecyclerView
    private lateinit var selectedCauses : MutableList<String>
    private var TAG = "CreateFundraiserActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_fundraiser)

        causesRecyclerView = findViewById(R.id.rvChooseFundraiserCauses)
        selectedCauses = mutableListOf()

        initCausesRecyclerView()

    }

    private fun initCausesRecyclerView() {
        /**
         * Initializes the causes recycler view in a Grid layout. The click listeners passed to the recyclerview adapter
         * handle user selection of a cause card and user de-selection of a cause card, respectively. They are passed into
         * recyclerview in this way so that clicks can manipulate the [selectedCauses] array, which is initialized and handled
         * here in the activity.
         */
        var causesArrayList : ArrayList<Cause> = arrayListOf()

        val causesItemAdapter = CausesGridItemAdapter(this, causesArrayList, object :
            OnUnselectedCauseClickListener {
            override fun onItemClick(cause: Cause?) {
                if (cause != null) {
                    cause.id?.let { selectedCauses.add(it) }
                    Log.d(TAG, "cause selected. selectedCauses: $selectedCauses")
                }
            }
        }, object : OnSelectedCauseClickListener {
            override fun onItemClick(cause: Cause?) {
                if (cause != null) {
                    cause.id?.let { selectedCauses.remove(it) }
                    Log.d(TAG, "cause unselected. selectedCauses: $selectedCauses")
                }
            }
        })

        // sets Recyclerview in a grid layout, 3 cards to a row
        GridLayoutManager(
            this.baseContext,
            3,
            RecyclerView.VERTICAL,
            false
        ).apply {
            causesRecyclerView.layoutManager = this
        }

        causesRecyclerView.adapter = causesItemAdapter

        // fixes uneven scrolling issue
        causesRecyclerView.setNestedScrollingEnabled(false)

        eventChangeListener(causesItemAdapter, causesArrayList)
    }

    private fun eventChangeListener(causesGridItemAdapter: CausesGridItemAdapter, causesArrayList: ArrayList<Cause>) {
        /**
         * Gets called at the end of Causes recyclerview initialization. Notifies Recyclerview adapter in order to fill recyclerview
         * with cards that each correspond to a cause, for user selection.
         */
        val db = FirebaseFirestore.getInstance()
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
                            var cause = dc.document.toObject(Cause::class.java)
                            cause.setUid(dc.document.id)
                            Log.d("Firestore debug", "cause object is : $cause")
                            causesArrayList.add(cause)
                        }
                    }
                    causesGridItemAdapter.notifyDataSetChanged()
                }
            })
    }

}