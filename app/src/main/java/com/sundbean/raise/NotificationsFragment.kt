import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.sundbean.raise.ChooseCausesActivity
import com.sundbean.raise.CreateEventActivity
import com.sundbean.raise.R
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.fragment_organize.*
import kotlinx.android.synthetic.main.fragment_organize.llCreateEventBtn

class NotificationsFragment:Fragment(R.layout.fragment_notifications) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnChooseCauses.setOnClickListener {
            val intent = Intent(activity, ChooseCausesActivity::class.java)
            activity!!.startActivity(intent)
        }
    }
}