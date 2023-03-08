package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.recentmodel.Tags
import com.luxpmsoft.luxaipoc.widget.TextViewFonts

class SelectTagAdapter(
    activity: Activity,
    resource: Int,
    tagsModel: ArrayList<Tags>,
    onListener: OnListener,
) :
    RecyclerView.Adapter<SelectTagAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val tagsModel: ArrayList<Tags> = tagsModel

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(tag: Tags)
        fun onDelete(position: Int, tagId: String, tagName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = tagsModel.get(position)
        try {
            if (model.name != null) {
                holder.tvTagItem.text = model.name
            }

            holder.setItemClickListener(object : SelectTagAdapter.ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    onListener.onListener(model)
                }
            })

            holder.ivDelete.setOnClickListener {
                onListener.onDelete(position, model.tagId!!, model.name!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var cbName: CheckBox
        var ivDelete: ImageView
        var tvTagItem: TextViewFonts

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
            cbName = itemView.findViewById(R.id.cbName)
            ivDelete = itemView.findViewById(R.id.ivDelete)
            tvTagItem = itemView.findViewById(R.id.tvTagItem)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return tagsModel.size
    }
}