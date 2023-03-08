package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.recentmodel.CadFileRows
import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import com.luxpmsoft.luxaipoc.utils.MyUtils
import de.hdodenhof.circleimageview.CircleImageView

class RecentFileAdapter(
    activity: Activity,
    resource: Int,
    recentFileModel: ArrayList<CadFileRows>,
    onListener: OnListener
) :
    RecyclerView.Adapter<RecentFileAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val recentFileModel: ArrayList<CadFileRows> = recentFileModel

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
        val model = recentFileModel.get(position)
        try {
            if(model.name != null) {
                holder.tvName.text = model.name
            }

            model.createdAt?.let {
                holder.tvTime.text = MyUtils.convertToLocalTime(it)
            }

            model.reconstructions?.let {
                if (it.isNotEmpty()) {
                    it[0]?.thumbnailImageKey?.let {
                        MyUtils.loadReconstructionImage(activity!!, (activity?.application as LidarApp).prefManager?.getToken(),
                            it, holder.iv_image, activity!!.resources.getDrawable(R.drawable.ic_model_scan))
                    }
                }
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
        var tvTime: TextView
        var imgAvatar: CircleImageView
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
            tvTime = itemView.findViewById(R.id.tvTime)
            imgAvatar = itemView.findViewById(R.id.imgAvatar)
            iv_image = itemView.findViewById(R.id.iv_image)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return recentFileModel.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}