package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.ConstantAPI
import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.PrefManager
import java.text.DecimalFormat

class RecentModelOrganizationAdapter(
    activity: Activity,
    resource: Int,
    model3D: ArrayList<Rows>,
    onListener: OnListener
) :
    RecyclerView.Adapter<RecentModelOrganizationAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val model3D: ArrayList<Rows> = model3D
    var prefManager: PrefManager? = PrefManager(activity)
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

            model.thumbnailImageKey?.let {
                if (it.isNotEmpty()) {
                    MyUtils.loadReconstructionImage(activity!!, (activity?.application as LidarApp).prefManager?.getToken(),
                        it, holder.iv_image, activity!!.resources.getDrawable(R.drawable.ic_model_scan))
                }
            }

            if(model.uploadDateTime != null) {
                holder.tvTime.text = model?.uploadDateTime?.let { MyUtils.convertDateTimeHH(it)}
            }

            model.fileSize?.let {
                if (it.isNotEmpty()) {
                    holder.tvSize.text = activity?.getString(R.string.size).plus(DecimalFormat("##.##").format(it.toDouble()/(1024*1024))).plus(" MB")
                }
            }

            model.scanningType?.scanningTypeName?.let {
                if (it.contains("Enhanced")){
                    holder.tvEnhanced.text = "Enhanced"
                }
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
        var tvSize: TextView
        var tvTime: TextView
        var tvEnhanced: TextView
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
            tvSize = itemView.findViewById(R.id.tvSize)
            tvTime = itemView.findViewById(R.id.tvTime)
            tvEnhanced = itemView.findViewById(R.id.tvEnhanced)
            iv_image = itemView.findViewById(R.id.iv_image)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return model3D.size
    }
}