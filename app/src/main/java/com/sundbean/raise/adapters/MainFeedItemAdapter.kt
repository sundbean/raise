package com.sundbean.raise.adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sundbean.raise.Opportunity
import com.sundbean.raise.R
import com.sundbean.raise.ui.EventDetailsActivity
import java.time.LocalTime

class MainFeedItemAdapter(private val modelList: ArrayList<Opportunity>, var context : Context) :
    RecyclerView.Adapter<MainFeedItemAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // I changed this from "parent.context" to "context" and I dont think it broke anything, but just a note for later in case it did...
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.large_event_card_layout, parent, false)
        return MyViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val opportunity : Opportunity = modelList[position]

        var time = opportunity.startTime
        var hour = time!!.get("hour")!!
        var minute = time!!.get("minute")!!
        var meridian = "AM"
        if (hour >= 12) {
            meridian = "PM"
            if (hour != 12) {
                hour -= 12
            }
        }

        var displayTime = LocalTime.of(hour, minute)

        holder.details.text = "${opportunity.date}  \u2022  $displayTime$meridian  \u2022  ${opportunity.rsvpNum} Going"

        if (opportunity.oppType == "event") {
            holder.oppIcon.setImageResource(R.drawable.ic_date)
            holder.name.text = "Event \u2022 ${opportunity.name}"
        } else if (opportunity.oppType == "fundraiser") {
            holder.oppIcon.setImageResource(R.drawable.ic_dollars)
            holder.name.text = "Fundraiser \u2022 ${opportunity.name}"
        }

        Glide.with(context!!).load(opportunity.photoUrl).centerCrop().into(holder.oppPhoto)

        // make it clickable - this is technically bad practice but I dont understand the better way: https://medium.com/android-gate/recyclerview-item-click-listener-the-right-way-daecc838fbb9
        holder.itemView.setOnClickListener {
            val intent = Intent(context, EventDetailsActivity::class.java)
            intent.putExtra("event_id", opportunity.id)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val oppPhoto : ImageView = itemView.findViewById(R.id.ivFeedCardPhoto)
        val oppIcon : ImageView = itemView.findViewById(R.id.ivOpportunityTypeIcon)
        val details : TextView = itemView.findViewById(R.id.tvFeedCardDetails)
        val name : TextView = itemView.findViewById(R.id.tvFeedCardTitle)
    }

}



// This is how the first video told me to do it

//class MainFeedItemAdapter(val modelList: ArrayList<Opportunity>) :
//    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        (holder as ViewHolder).bind(modelList.get(position));
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val layoutInflater = LayoutInflater.from(parent.context)
//        return ViewHolder(layoutInflater.inflate(R.layout.feed_card_layout, parent, false))
//    }
//
//    override fun getItemCount(): Int {
//        return modelList.size;
//    }
//
//    lateinit var mClickListener: ClickListener
//
//    fun setOnClickListener(aClickListener: ClickListener) {
//        mClickListener = aClickListener
//    }
//
//    interface ClickListener {
//        fun onClick(pos: Int, aView: View)
//    }
//
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnClickListener{
//
//        init {
//            itemView.setOnClickListener(this)
//        }
//
//        fun bind(model: Opportunity): Unit {
//            // set card icon according to opportunity type
//            if (model.type == "event") {
//                itemView.ivOpportunityTypeIcon.setBackgroundResource(R.drawable.ic_date)
//            } else if (model.type == "fundraiser") {
//                itemView.ivOpportunityTypeIcon.setBackgroundResource(R.drawable.ic_dollars)
//            } else if (model.type == "group") {
//                itemView.ivOpportunityTypeIcon.setBackgroundResource(R.drawable.ic_group)
//            }
//
//            // set card textual details
//            itemView.tvFeedCardTitle.text = "${model.type?.capitalize()} \u2022 ${model.title}"
//            itemView.tvFeedCardDetails.text = "${model.date} \u2022 ${model.time} \u2022 ${model.attendeeNum as String?} Going"
//
//            // set card image
////            Glide.with(context!!).load(model.imgUrl).into(itemView.ivFeedCardPhoto)
//        }
//
//        override fun onClick(p0: View?) {
//            mClickListener.onClick(adapterPosition, itemView)
//        }
//    }
//}
