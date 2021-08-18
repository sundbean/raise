package com.sundbean.raise.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.sundbean.raise.R
import kotlinx.android.synthetic.main.fragment_explore.*


class ExploreFragment:Fragment(R.layout.fragment_explore) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        initSearch()

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

}