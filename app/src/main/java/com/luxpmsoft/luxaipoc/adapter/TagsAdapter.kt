package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.recentmodel.Tags

class TagsAdapter(
    activity: Activity,
    resource: Int,
    tagsModel: ArrayList<Tags>,
    onListener: OnListener,
    val isShow: Boolean? = false
) :
    RecyclerView.Adapter<TagsAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val tagsModel: ArrayList<Tags> = tagsModel

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(tag: Tags)
        fun onEdit(position: Int, tagId: String, tagName: String)
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
            if(model.name != null) {
                holder.tvName.text = model.name
                holder.tvEditName.setText(model.name)
            }

            if (model.isEdit == true) {
                holder.tvEditName.visibility = View.VISIBLE
                holder.tvName.visibility = View.GONE
                holder.ivEdit.visibility = View.GONE
                holder.ivSave.visibility = View.VISIBLE
            } else {
                holder.tvEditName.visibility = View.GONE
                holder.tvName.visibility = View.VISIBLE
                holder.ivEdit.visibility = View.VISIBLE
                holder.ivSave.visibility = View.GONE
            }

//            holder.setItemClickListener(object : ItemClickListener {
//                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
//                }
//            })

            if (isShow == true) {
                holder.ivEdit.visibility = View.GONE
                holder.ivSave.visibility = View.GONE
                holder.ivDelete.visibility = View.GONE
            }

            holder.ivEdit.setOnClickListener {
                holder.tvEditName.visibility = View.VISIBLE
                holder.tvName.visibility = View.GONE
                holder.ivEdit.visibility = View.GONE
                holder.ivSave.visibility = View.VISIBLE
            }

            holder.setItemClickListener(object : TagsAdapter.ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    if (isShow == true) {
                        onListener.onListener(model)
                    }
                }
            })

            holder.ivSave.setOnClickListener {
                if (holder.tvEditName.text.isNotEmpty()) {
                    onListener.onEdit(position, model.tagId!!, holder.tvEditName.text.toString())
                }
            }

            holder.ivDelete.setOnClickListener {
                onListener.onDelete(position, model.tagId!!, model.name!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var tvName: TextView
        var ivEdit: ImageView
        var ivDelete: ImageView
        var ivSave: ImageView
        var tvEditName: EditText
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
            ivEdit = itemView.findViewById(R.id.ivEdit)
            ivDelete = itemView.findViewById(R.id.ivDelete)
            ivSave = itemView.findViewById(R.id.ivSave)
            tvEditName = itemView.findViewById(R.id.tvEditName)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return tagsModel.size
    }
}