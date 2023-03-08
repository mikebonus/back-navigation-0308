package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.listfilescan.FileScanModel
import com.luxpmsoft.luxaipoc.view.activity.ListImageActivity
import com.luxpmsoft.luxaipoc.view.activity.ProgressUploadingActivity

class ListFileScanAdapter(
    activity: Activity,
    resource: Int,
    listImage: ArrayList<FileScanModel>,
    val isScene: Boolean = false
) :
    RecyclerView.Adapter<ListFileScanAdapter.RecyclerViewHolder>() {
    var activity: Activity? = activity
    var resource = resource
    val listImage: ArrayList<FileScanModel> = listImage

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
            if(model.thumbnail != null) {
                Glide.with(activity!!)
                    .load(model.thumbnail)
                    .centerCrop()
                    .into(holder.ivImage)
            }

            holder.tvName.text = model.name
            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    if (isScene) {
                        val intent = Intent(activity, ProgressUploadingActivity::class.java)
                        intent.putExtra("pathFile", model.url)
                        activity?.startActivity(intent)
                    } else {
                        val intent = Intent(activity, ListImageActivity::class.java)
                        intent.putExtra("pathFile", model.url)
                        activity?.startActivity(intent)
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var ivImage: ImageView
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
            ivImage = itemView.findViewById(R.id.iv_image)
            tvName = itemView.findViewById(R.id.tvName)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return listImage.size
    }
}