package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.CameraConfig
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.ResolutionModel

class ResolutionAdapter (
    activity: Activity,
    resource: Int,
    resolutionList: ArrayList<CameraConfig>,
    private val listener: IAdapterClickListener
) :
    RecyclerView.Adapter<ResolutionAdapter.RecyclerViewHolder>() {
    var activity: Activity? = activity
    var resource = resource
    val resolutionList: ArrayList<CameraConfig> = resolutionList

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = resolutionList.get(position)
        try {
            holder.tvName.text = model.imageSize.width.toString().plus("x"+model.imageSize.height.toString())
            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    listener.onClickListener(model)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var tvName: TextView
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
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return resolutionList.size
    }

    interface IAdapterClickListener {
        fun onClickListener(cameraConfig: CameraConfig)
    }
}