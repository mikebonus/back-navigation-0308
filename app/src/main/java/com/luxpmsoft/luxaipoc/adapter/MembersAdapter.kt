package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.MembersModel

class MembersAdapter(
    activity: Activity,
    resource: Int,
    model3D: ArrayList<MembersModel>,
    onListener: OnListener
) :
    RecyclerView.Adapter<MembersAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val model3D: ArrayList<MembersModel> = model3D

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(model: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = model3D.get(position)
        try {
            if(model.name != null) {
                holder.tvName.text = model.name
            }

            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    onListener.onListener(model.name!!)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var tvName: TextView
        var iv_image: ImageView
        //        var icFavorite: ImageView
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
            iv_image = itemView.findViewById(R.id.iv_image)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return model3D.size
    }
}