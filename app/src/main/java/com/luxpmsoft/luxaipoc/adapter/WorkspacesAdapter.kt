package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspacesModel

class WorkspacesAdapter (
    activity: Activity,
    resource: Int,
    workspacesModel: ArrayList<WorkspacesModel>,
    onListener: OnListener
) :
    RecyclerView.Adapter<WorkspacesAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val workspacesModel: ArrayList<WorkspacesModel> = workspacesModel

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(workspaceId: String)
        fun onCreateProject(workspaceId: String, workspaceName: String, view: View?, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = workspacesModel.get(position)
        try {
            if(model.workspaceName != null) {
                holder.tvName.text = model.workspaceName
            }

            holder.tvDescription.setText("")
            model.description?.let {
                holder.tvDescription.text = model.description
            }

            model.folders?.let {
                holder.tvTotalProject.text = it.size.toString()
                var b= 0
                for (board in it) {
                    board?.boards?.also {
                        b= b + it.size
                    }
                }
                holder.tvTotalBoard.text = b.toString()
            }

            holder.icMore.setOnClickListener {
                onListener.onCreateProject(model.workspaceId!!, model.workspaceName!!, holder.icMore, position)
            }

            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    onListener.onListener(model.workspaceId!!)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var tvName: TextView
        var tvTotalProject: TextView
        var tvTotalBoard: TextView
        var tvDescription: TextView
        var icMore: ImageView
        //        var icFavorite: ImageView
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
            tvTotalProject = itemView.findViewById(R.id.tvTotalProject)
            tvTotalBoard = itemView.findViewById(R.id.tvTotalBoard)
            tvDescription = itemView.findViewById(R.id.tvDescription)
            icMore = itemView.findViewById(R.id.icMore)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return workspacesModel.size
    }
}