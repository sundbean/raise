package com.sundbean.raise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.feed_card_layout.view.*

class MainFeedItemAdapter(private val modelList: ArrayList<Opportunity>, var context : Context) :
    RecyclerView.Adapter<MainFeedItemAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainFeedItemAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.feed_card_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MainFeedItemAdapter.MyViewHolder, position: Int) {
        val opportunity : Opportunity = modelList[position]

        holder.details.text = "${opportunity.date} \u2022 ${opportunity.time}"

        if (opportunity.type == "event") {
            holder.oppIcon.setBackgroundResource(R.drawable.ic_date)
            holder.name.text = "Event \u2022 ${opportunity.name}"
        } else if (opportunity.type == "fundraiser") {
            holder.oppIcon.setBackgroundResource(R.drawable.ic_dollars)
            holder.name.text = "Fundraiser \u2022 ${opportunity.name}"
        }

        Glide.with(context!!).load(opportunity.photoUrl).into(holder.oppPhoto)
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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
