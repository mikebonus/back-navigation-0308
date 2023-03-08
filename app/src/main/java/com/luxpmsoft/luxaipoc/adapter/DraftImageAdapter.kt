package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.model.listimage.DraftImageModel

class DraftImageAdapter(
    activity: Activity,
    resource: Int,
    listImage: ArrayList<DraftImageModel>
) :
    RecyclerView.Adapter<DraftImageAdapter.RecyclerViewHolder>() {
    var activity: Activity? = activity
    var resource = resource
    val listImage: ArrayList<DraftImageModel> = listImage

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
                Utils.loadImage(activity, model.url, holder.imv_preview)
            }
            if(model.total != null) {
                holder.tvNumberImage.text = model.total.toString()
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
        var imv_preview: ImageView
        var tvNumberImage: TextView

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
            imv_preview = itemView.findViewById(R.id.imv_preview)
            tvNumberImage = itemView.findViewById(R.id.tvNumberImage)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return listImage.size
    }
}