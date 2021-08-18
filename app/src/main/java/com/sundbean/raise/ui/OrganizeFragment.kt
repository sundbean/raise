package com.sundbean.raise.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.sundbean.raise.*
import kotlinx.android.synthetic.main.fragment_organize.*

class OrganizeFragment:Fragment(R.layout.fragment_organize) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        llCreateEventBtn.setOnClickListener {
            val intent = Intent(activity, CreateEventActivity::class.java)
            requireActivity().startActivity(intent)
        }

        llCreateFundraiserBtn.setOnClickListener {
            val intent = Intent(activity, CreateFundraiserActivity::class.java)
            requireActivity().startActivity(intent)
        }

        llCreateGroupBtn.setOnClickListener {
            val intent = Intent(activity, CreateGroupActivity::class.java)
            requireActivity().startActivity(intent)
        }

    }
}