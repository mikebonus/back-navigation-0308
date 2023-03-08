package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.SortModel

class SortAdapter(
    activity: Activity,
    resource: Int,
    sort: ArrayList<SortModel>,
    private val listener: IAdapterClickListener
) :
    RecyclerView.Adapter<SortAdapter.RecyclerViewHolder>() {
    var activity: Activity? = activity
    var resource = resource
    val sort: ArrayList<SortModel> = sort

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = sort.get(position)
        try {
//            model.name?.let {
//                holder.tvName.text = it
//            }

            model.localizedName?.let {
                holder.tvName.text = it
            }

            holder.tvName.setBackgroundColor(activity!!.resources.getColor(R.color.colorTransparent))
            model.isCheck?.let {
                if(it) {
                    holder.tvName.background = activity!!.resources.getDrawable(R.drawable.bg_purple1_12)
                }
            }
            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    listener.onSelect(position, true)
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
        return sort.size
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