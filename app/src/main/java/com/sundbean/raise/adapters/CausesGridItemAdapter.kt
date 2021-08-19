package com.sundbean.raise.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sundbean.raise.R
import com.sundbean.raise.models.Cause
import com.sundbean.raise.models.OnSelectedCauseClickListener
import com.sundbean.raise.models.OnUnselectedCauseClickListener

/**
 * This adapter is intended for a causes grid layout recyclerview, where what happens when a card is clicked depends on if it has
 * already been selected.
 */

class CausesGridItemAdapter(private val context : Context, private val causesList: MutableList<Cause>, private val notSelectedListener: OnUnselectedCauseClickListener, private val selectedListener: OnSelectedCauseClickListener) : RecyclerView.Adapter<CausesGridItemAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        // inflate the custom view from xml layout file
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.cause_card_layout,parent,false)

        // return the view holder
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val cause : Cause = causesList[position]
        Glide.with(context!!).load(cause.photoUrl).into(holder.ivCausePhoto)


        holder.itemView.setOnClickListener {
            if (holder.ivCausePhoto.colorFilter == null) {
                // do this if the photo has no color filter; i.e. has not been selected
                notSelectedListener.onItemClick(cause)
                holder.ivCausePhoto.setColorFilter(Color.argb(70, 0, 120, 255))
            } else {
                selectedListener.onItemClick(cause)
                holder.ivCausePhoto.colorFilter = null
            }
        }
    }


    override fun getItemCount(): Int {
        // number of items in the data set held by the adapter
        return causesList.size
    }


    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
//        fun bind(cause: Cause, listener: OnItemClickListener) {
//            itemView.setOnClickListener { listener.onItemClick(cause) }
//        }

        val ivCausePhoto : ImageView = itemView.findViewById(R.id.ivCause)
    }


    // these two methods are useful for avoiding duplicate items
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }
}