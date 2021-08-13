import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.google.type.Date
import com.google.type.DateTime
import com.sundbean.raise.*
import com.sundbean.raise.R
import kotlinx.android.synthetic.main.fragment_main_feed2.*
import kotlinx.android.synthetic.main.fragment_organize.*
import java.util.*
import java.util.EventListener

class MainFeedFragment:Fragment(R.layout.fragment_main_feed2) {

    private lateinit var recyclerView : RecyclerView
    private lateinit var opportunityArrayList: ArrayList<Opportunity>
    private lateinit var feedItemAdapter: MainFeedItemAdapter
    private lateinit var db: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvMainFeed)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        opportunityArrayList = arrayListOf()

        feedItemAdapter = MainFeedItemAdapter(opportunityArrayList, requireActivity())

        recyclerView.adapter = feedItemAdapter

        eventChangeListener()


//        var date = "2021-10-12, 14:50"
//        var dateTime = java.util.Date(2021, 12, 12, 16, 40)
//        Log.d("MainFeedFragment", "dateTime: $dateTime")
//        tvDateDisplayTest.text = dateTime.toString()

        // An approach for dealing with dates:
        // Grab the year, month, day, hour, and minute from the date picker and time picker and store each in its own variable
        // Pass these variables to a "time conversion" class (that I need to make), which will:
        // // - if the functionality exists, create a timestamp directly from these variables
        // // - as a last resort, simply join them together in a string like "2021-10-12-16-45" and store the string as the "dateAndTime"
        // // // - if I do this, I can then store the "duration" as another field (value in minutes)
        // // // - in this format it will be easier to figure out dates in a certain provided time range
        // // // - then when i need to display hte dates, I can parse out the data from the string (possibly splitting at "-" and putting the values in an array) to display in my desired format


//        llCreateEventBtn.setOnClickListener {
//            val intent = Intent(activity, CreateEventActivity::class.java)
//            requireActivity().startActivity(intent)
//        }
//
//        llCreateFundraiserBtn.setOnClickListener {
//            val intent = Intent(activity, CreateFundraiserActivity::class.java)
//            requireActivity().startActivity(intent)
//        }
//
//        llCreateGroupBtn.setOnClickListener {
//            val intent = Intent(activity, CreateGroupActivity::class.java)
//            requireActivity().startActivity(intent)
//        }
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
                            opportunityArrayList.add(dc.document.toObject(Opportunity::class.java))
                        }
                    }
                    feedItemAdapter.notifyDataSetChanged()
                }
            })
    }
}

