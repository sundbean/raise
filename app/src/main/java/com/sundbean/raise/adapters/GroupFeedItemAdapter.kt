package com.sundbean.raise.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sundbean.raise.R
import com.sundbean.raise.models.Group
import com.sundbean.raise.ui.GroupDetailsActivity

class GroupFeedItemAdapter(private val modelList: ArrayList<Group>, var context : Context) :
    RecyclerView.Adapter<GroupFeedItemAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.small_group_card_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val group : Group = modelList[position]

        holder.name.text = "${group.name}"

        Glide.with(context!!).load(group.photoUrl).centerCrop().into(holder.photo)

        // make it clickable - this is technically bad practice but I dont understand the better way: https://medium.com/android-gate/recyclerview-item-click-listener-the-right-way-daecc838fbb9
        holder.itemView.setOnClickListener {
            val intent = Intent(context, GroupDetailsActivity::class.java)
            intent.putExtra("group_id", group.id)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val photo : ImageView = itemView.findViewById(R.id.ivGroupSmallCardPhoto)
        val name : TextView = itemView.findViewById(R.id.tvGroupSmallCardTitle)
    }

}