package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.Country

class CountryAdapter(
    activity: Activity,
    resource: Int,
    country :ArrayList<Country>,
    private val listener: OnListener
) :
    RecyclerView.Adapter<CountryAdapter.RecyclerViewHolder>() {
    var activity: Activity? = activity
    var resource = resource
    val country: ArrayList<Country> = country

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(country: Country)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = country.get(position)
        try {
            if (model.name != null) {
                holder.tvName.text = model.name
            }

            if (model.callingCode != null) {
                holder.tvCode.text = model.callingCode
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
                    listener.onListener(model)

                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var tvName: TextView
        var tvCode: TextView
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
            tvName = itemView.findViewById(R.id.tvName)
            tvCode = itemView.findViewById(R.id.tvCode)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return country.size
    }
}