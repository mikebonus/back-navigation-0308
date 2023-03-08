package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.PrefManager
import java.text.DecimalFormat

class SelectModelsAdapter (
    activity: Activity,
    resource: Int,
    listModel: ArrayList<Rows>,
    private val listener: IAdapterClickListener
) :
    RecyclerView.Adapter<SelectModelsAdapter.RecyclerViewHolder>() {
    var activity: Activity? = activity
    var resource = resource
    val listModel: ArrayList<Rows> = listModel
    var prefManager: PrefManager? = PrefManager(activity)

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = listModel.get(position)
        try {
            model.modelName?.let {
                holder.tvModelName.text = model.modelName
            }

            model.fileSize?.let {
                holder.tvSize.text = activity?.getString(R.string.size).plus(DecimalFormat("##.##").format(it.toDouble()/(1024*1024))).plus(" MB")
            }

            model.thumbnailImageKey?.let {
                if (it.isNotEmpty()) {
                    activity?.let { it1 -> MyUtils.loadReconstructionImage(it1, prefManager?.getToken()!!, it, holder.ivImage,
                        activity!!.resources.getDrawable(R.drawable.ic_model_scan)) }
                }
            }

            model.isCheck?.let {
                holder.ckbAccept.isChecked = it
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
        var tvModelName: TextView
        var tvSize: TextView
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
            tvModelName = itemView.findViewById(R.id.tvModelName)
            tvSize = itemView.findViewById(R.id.tvSize)
            ivImage = itemView.findViewById(R.id.iv_image)
            ckbAccept = itemView.findViewById(R.id.ckbAccept)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return listModel.size
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