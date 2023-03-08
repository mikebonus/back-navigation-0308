package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.defect_detect.TestedModelsData
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.PrefManager

class TestedModelAdapter (
    activity: Activity,
    resource: Int,
    testedModels: ArrayList<TestedModelsData>,
    onListener: OnListener
) :
    RecyclerView.Adapter<TestedModelAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val testedModels: ArrayList<TestedModelsData> = testedModels
    var prefManager: PrefManager? = PrefManager(activity)
    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = testedModels.get(position)
        try {
            model.test_name?.let {
                holder.tvName.text = it
            }

//            model.thumbnail?.let {
//                MyUtils.loadDefectImage(activity!!, it, holder.iv_image, activity!!.resources.getDrawable(
//                    R.drawable.ic_empty))
//            }

            model.updated_at?.let {
                holder.tvTime.text = MyUtils.convertToLocalTime1(it)
            }

            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    onListener.onListener(position)

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
            iv_image = itemView.findViewById(R.id.iv_image)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return testedModels.size
    }
}