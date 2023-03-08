package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.Country
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionType

class PricingPlanAdapter(
    activity: Activity,
    resource: Int,
    subscriptionType :ArrayList<SubscriptionType>,
    private val listener: OnListener
) :
    RecyclerView.Adapter<PricingPlanAdapter.RecyclerViewHolder>() {
    var activity: Activity? = activity
    var resource = resource
    val subscriptionType: ArrayList<SubscriptionType> = subscriptionType

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(position: Int, sub: SubscriptionType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = subscriptionType.get(position)
        try {
            if(model.subscriptionName != null) {
                holder.tvSubscriptionName.text = model.subscriptionName
            }

            if(model.price != null) {
                holder.tvPrice.text = model.price
            }

            if(model.totalData != null) {
                holder.tvStorage.text = model.totalData.plus(" GB*")
            }

            if(model.isChoose == true) {
                if (model.type?.contains("Individual")!!) {
                    holder.lineRoot.background = activity?.resources?.getDrawable(R.drawable.bg_blue_pink_14)
                } else {
                    holder.lineRoot.background = activity?.resources?.getDrawable(R.drawable.bg_gradient_orange_purple_14)
                }
            } else {
                if (model.type?.contains("Individual")!!) {
                    holder.lineRoot.background = activity?.resources?.getDrawable(R.drawable.bg_pricing_personal)
                } else {
                    holder.lineRoot.background = activity?.resources?.getDrawable(R.drawable.bg_pricing_organization)
                }
            }
//            try {
//                Glide.with(activity!!)
//                    .load(glideUrl)
//                    .centerCrop()
//                    .into(holder.iv_image)
//            } catch (e: Exception) {
//                e.message
//            }

            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    listener.onListener(position, model)

                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var tvSubscriptionName: TextView
        var tvStorage: TextView
        var tvPrice: TextView
        var lineRoot: LinearLayoutCompat
        private var itemClickListener: ItemClickListener? = null
        fun setItemClickListener(itemClickListener: ItemClickListener?) {
            this.itemClickListener = itemClickListener
        }

        override fun onClick(v: View) {
            try {
                itemClickListener!!.onClick(v, adapterPosition, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            tvSubscriptionName = itemView.findViewById(R.id.tvSubscriptionName)
            tvStorage = itemView.findViewById(R.id.tvStorage)
            tvPrice = itemView.findViewById(R.id.tvPrice)
            lineRoot = itemView.findViewById(R.id.lineRoot)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return subscriptionType.size
    }
}