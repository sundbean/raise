package com.sundbean.raise

import android.content.Context
import android.media.Image
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.custom_view.view.*
import java.net.URI

class RecyclerViewAdapter(private val c : Context, private val causes: MutableList<String>) : RecyclerView.Adapter<RecyclerViewAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        // inflate the custom view from xml layout file
        val view: View = LayoutInflater.from(c)
            .inflate(R.layout.custom_view,parent,false)

        // return the view holder
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        // display current cause
        val imageUri = causes[position]
        val imageResource = c.resIdByName(imageUri, "mipmap")
        holder.iv.setImageResource(imageResource)
    }


    override fun getItemCount(): Int {
        // number of items in the data set held by the adapter
        return causes.size
    }


    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val iv = itemView.iv as ImageView
    }


    // this two methods useful for avoiding duplicate item
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }
}