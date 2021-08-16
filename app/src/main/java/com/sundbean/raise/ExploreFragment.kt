import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.sundbean.raise.EventConfirmationActivity
import com.sundbean.raise.EventDetailsActivity
import com.sundbean.raise.GroupDetailsActivity
import com.sundbean.raise.R
import kotlinx.android.synthetic.main.fragment_explore.*

class ExploreFragment:Fragment(R.layout.fragment_explore) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        btnGoToEventDetails.setOnClickListener {
            val intent = Intent(activity, EventDetailsActivity::class.java)
            intent.putExtra("event_id", "AkVDkIakRFnPlEyzmwCQ")
            startActivity(intent)
        }

        btnGoToGroupDetails.setOnClickListener {
            val intent = Intent(activity, GroupDetailsActivity::class.java)
            startActivity(intent)
        }

    }

}