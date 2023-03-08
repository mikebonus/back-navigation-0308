package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.listimage.ListImageModel
import com.bumptech.glide.Glide
import com.google.ar.core.CameraConfig


class ListImageAdapter(
    activity: Activity,
    resource: Int,
    listImage: ArrayList<ListImageModel>,
    private val listener: IAdapterClickListener
) :
    RecyclerView.Adapter<ListImageAdapter.RecyclerViewHolder>() {
    var activity: Activity? = activity
    var resource = resource
    val listImage: ArrayList<ListImageModel> = listImage

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = listImage.get(position)
        try {
            if(model.url != null) {
                Glide.with(activity!!)
                    .load(model.url)
                    .centerCrop()
                    .into(holder.ivImage)
            }
            holder.icDelete.setOnClickListener {
                listener.onRemove(position)
            }
            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var ivImage: ImageView
        var icDelete: ImageView
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
            ivImage = itemView.findViewById(R.id.iv_image)
            icDelete = itemView.findViewById(R.id.icDelete)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return listImage.size
    }

    interface IAdapterClickListener {
        fun onRemove(position: Int)
    }
}