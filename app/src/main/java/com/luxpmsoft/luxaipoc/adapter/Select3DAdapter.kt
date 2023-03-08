package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.utils.MyUtils

class Select3DAdapter(
    activity: Activity,
    resource: Int,
    model3D: ArrayList<Rows>,
    onListener: OnListener
) :
    RecyclerView.Adapter<Select3DAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val model3D: ArrayList<Rows> = model3D
    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(model: String, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = model3D.get(position)
        try {
            if(model.modelName != null) {
                holder.tvName.text = model.modelName
            }

            if(model.uploadDateTime != null) {
                holder.tvType.text = model?.uploadDateTime?.let { MyUtils.convertToLocalTime(it)}
            }

            model.scanningType?.let {
                holder.tvTypeEnhanced.text = it.scanningTypeName
            }
            try {
                model.thumbnailImageKey?.let {
                    if (it.isNotEmpty()) {
                        MyUtils.loadReconstructionImage(activity!!, (activity?.application as LidarApp).prefManager?.getToken(),
                            it, holder.iv_image, activity!!.resources.getDrawable(R.drawable.ic_model_scan))
                    }
                }
            } catch (e: Exception) {
                e.message
            }

            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    onListener.onListener(model.modelName!!, position)

                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var tvName: TextView
        var tvType: TextView
        var tvTypeEnhanced: TextView
        var iv_image: ImageView
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
            tvType = itemView.findViewById(R.id.tvType)
            tvTypeEnhanced = itemView.findViewById(R.id.tvTypeEnhanced)
            iv_image = itemView.findViewById(R.id.iv_image)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return model3D.size
    }
}