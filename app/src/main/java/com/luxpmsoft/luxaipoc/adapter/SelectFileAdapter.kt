package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.recentmodel.CadFileRows
import com.luxpmsoft.luxaipoc.utils.MyUtils

class SelectFileAdapter (
    activity: Activity,
    resource: Int,
    listFiles: ArrayList<CadFileRows>,
    private val listener: IAdapterClickListener
) :
    RecyclerView.Adapter<SelectFileAdapter.RecyclerViewHolder>() {
    var activity: Activity? = activity
    var resource = resource
    val listFiles: ArrayList<CadFileRows> = listFiles

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = listFiles.get(position)
        try {
            model.reconstructions?.let {
                if (it.isNotEmpty()) {
                    it[0].thumbnailImageKey?.let {
                        MyUtils.loadReconstructionImage(activity!!, (activity?.application as LidarApp).prefManager?.getToken(),
                            it, holder.ivImage, activity!!.resources.getDrawable(R.drawable.ic_empty))
                    }
                }
            }
            holder.ckbAccept.setOnClickListener {
                listener.onSelect(position, holder.ckbAccept.isChecked)
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
        var ckbAccept: CheckBox
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
            ckbAccept = itemView.findViewById(R.id.ckbAccept)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return listFiles.size
    }

    interface IAdapterClickListener {
        fun onSelect(position: Int, isCheck: Boolean)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}