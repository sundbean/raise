package com.sundbean.raise.ui

import android.content.Intent
import androidx.fragment.app.Fragment
import com.sundbean.raise.R
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment:Fragment(R.layout.fragment_profile) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        ibSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            requireActivity().startActivity(intent)
        }

    }


}