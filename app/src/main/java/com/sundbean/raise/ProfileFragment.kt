import android.content.Intent
import androidx.fragment.app.Fragment
import com.sundbean.raise.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sundbean.raise.SettingsActivity
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment:Fragment(R.layout.fragment_profile) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        btnSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            activity!!.startActivity(intent)
        }

    }


}