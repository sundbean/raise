package com.sundbean.raise.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sundbean.raise.R
import com.sundbean.raise.models.Cause

class CausesFeedItemAdapter(private val context : Context, private val causesList: MutableList<Cause>) : RecyclerView.Adapter<CausesFeedItemAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        // inflate the custom view from xml layout file
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.tall_cause_card_layout,parent,false)

        // return the view holder
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val cause : Cause = causesList[position]
        Glide.with(context!!).load(cause.photoUrl).centerCrop().into(holder.ivTallCausePhoto)
    }


    override fun getItemCount(): Int {
        // number of items in the data set held by the adapter
        return causesList.size
    }


    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val ivTallCausePhoto : ImageView = itemView.findViewById(R.id.ivTallCauseImage)
    }


    // these two methods are useful for avoiding duplicate items
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }
}