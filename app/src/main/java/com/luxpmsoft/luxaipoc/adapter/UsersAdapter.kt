package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.home.User
import com.luxpmsoft.luxaipoc.model.repositories.OrganizationUser
import com.luxpmsoft.luxaipoc.model.repositories.RepositoryUsers
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.PrefManager

class UsersAdapter (
    activity: Activity,
    resource: Int,
    userModel: ArrayList<OrganizationUser>,
    onListener: OnListener,
    var isDelete: Boolean = false
) :
    RecyclerView.Adapter<UsersAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val userModel: ArrayList<OrganizationUser> = userModel
    var prefManager: PrefManager? = PrefManager(activity)

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(repositoryId: String, repositoryName: String)
        fun onDelete(userId: String, userName: String, position: Int)
        fun onSelect(position: Int, isCheck: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = userModel.get(position)
        try {
            if (isDelete) {
                holder.icDelete.visibility = View.VISIBLE
                holder.ckbAccept.visibility = View.GONE
            } else {
                holder.icDelete.visibility = View.GONE
                holder.ckbAccept.visibility = View.VISIBLE
            }
            model.user?.let { user ->
                holder.imgAvatar.setImageDrawable(MyUtils.convertViewToBitmap(activity!!, user.full_name!!.substring(0, 1).uppercase()))
                holder.tvUserName.text = user.full_name
                user.profileImageKey?.let {
                    MyUtils.loadAvatar(activity!!, prefManager?.getToken(), it,
                        holder.imgAvatar, MyUtils.convertViewToBitmap(activity!!, user.full_name!!.substring(0, 1).uppercase()))
                }
            }

            model.user?.uid.let {
                if (isDelete) {
                    if ( prefManager?.getUserId() == it) {
                        holder.icDelete.visibility = View.GONE
                        holder.tvUserName.text = "You"
                    } else {
                        holder.icDelete.visibility = View.VISIBLE
                    }
                }
            }

            model.role?.let {
                holder.tvRole.text = it.substring(0, 1).toUpperCase() + it.substring(1).toLowerCase()
            }

            model.isCheck?.let {
                holder.ckbAccept.isChecked = it
            }

            holder.icDelete.setOnClickListener {
                model.repositoryUserId?.let { it1 -> onListener.onDelete(it1, model.user?.full_name.toString(), position) }
            }

            holder.ckbAccept.setOnClickListener {
                onListener.onSelect(position, holder.ckbAccept.isChecked)
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
        var tvUserName: TextView
        var tvRole: TextView
        var imgAvatar: ImageView
        var icDelete: ImageView
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
            tvUserName = itemView.findViewById(R.id.tvUserName)
            tvRole = itemView.findViewById(R.id.tvRole)
            imgAvatar = itemView.findViewById(R.id.imgAvatar)
            icDelete = itemView.findViewById(R.id.icDelete)
            ckbAccept = itemView.findViewById(R.id.ckbAccept)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return userModel.size
    }
}