package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.notification.NotificationData
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.PrefManager

class NotificationAdapter (
    activity: Activity,
    resource: Int,
    notification: ArrayList<NotificationData>,
    onListener: OnListener
) :
    RecyclerView.Adapter<NotificationAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val notification: ArrayList<NotificationData> = notification
    var prefManager: PrefManager? = PrefManager(activity)
    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(model: String, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = notification.get(position)
        try {"This is <font color='red'>simple</font>."
            model.notificationData?.message?.let {
                holder.tvName.text = it
            }

            model.user?.profileImageKey?.let {
                MyUtils.loadAvatar(activity!!, (activity?.application as LidarApp).prefManager?.getToken(), it,
                    holder.imgAvatar, activity!!.resources.getDrawable(R.drawable.user))
            }

            model.createdAt?.let {
                holder.tvTime.text = MyUtils.convertDateTimeHH(it)
            }

            model.notificationState?.let {
                if (it.contains("unread")) {
                    holder.lineDot.visibility = View.VISIBLE
                } else {
                    holder.lineDot.visibility = View.INVISIBLE
                }
            }

            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    onListener.onListener(model.notificationData?.message.toString(), position)
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
        var imgAvatar: ImageView
        var lineDot: LinearLayoutCompat
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
            lineDot = itemView.findViewById(R.id.lineDot)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return notification.size
    }
}