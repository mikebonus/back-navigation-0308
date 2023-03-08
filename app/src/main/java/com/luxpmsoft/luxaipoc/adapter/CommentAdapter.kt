package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.recentmodel.Comments
import com.luxpmsoft.luxaipoc.model.recentmodel.Tags
import com.luxpmsoft.luxaipoc.utils.MyUtils

class CommentAdapter (
    activity: Activity,
    resource: Int,
    commentModel: ArrayList<Comments>,
    onListener: OnListener,
    var isReply: Boolean = false
) :
    RecyclerView.Adapter<CommentAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val commentModel: ArrayList<Comments> = commentModel

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(comment: Comments)
        fun onAddTagComment(comment: Comments, view: View?)
        fun onRemoveTagComment(comment: Comments)
        fun onDeleteComment(comment: Comments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = commentModel.get(position)
        try {
            if (isReply) {
                holder.lineReply.visibility = View.VISIBLE
                holder.ivTag.visibility = View.VISIBLE
            } else {
                holder.lineReply.visibility = View.GONE
                holder.ivTag.visibility = View.GONE
            }

                model.text?.let {
                    holder.tvCommentLeft.text = it
                }

                model.user?.first_name?.let {
                    holder.tvUserNameLeft.text = it
                }
                model.createdAt?.let {
                    holder.tvTimeLeft.text = MyUtils.convertToLocalTime(it)
                }
                model.user?.profileImageKey?.let {
                    MyUtils.loadAvatar(
                        activity!!, (activity?.application as LidarApp).prefManager!!.getToken(),
                        it, holder.imgAvatarLeft, activity!!.resources.getDrawable(R.drawable.user))
                }

                holder.tvTotalReplyLeft.text = "0 ".plus(activity?.getString(R.string.reply))
                model.comments?.let {
                    if (it.isNotEmpty()) {
                        holder.tvTotalReplyLeft.text = it.size.toString().plus(" ").plus(activity!!.getString(R.string.reply))
                    }
                }

                holder.lineTag.visibility = View.GONE
                holder.tagName.text = ""

                model.tags?.let {
                    if (it.isNotEmpty()) {
                        holder.lineTag.visibility = View.VISIBLE
                        holder.ivTag.visibility = View.GONE
                        holder.tagName.text = it[0].name
                    }
                }

            holder.icRemoveTag.setOnClickListener {
                onListener.onRemoveTagComment(model)
            }

            holder.tagName.setOnClickListener {

            }

            holder.ivDeleteComment.setOnClickListener {
                onListener.onDeleteComment(model)
            }

            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    onListener.onListener(model)
                }
            })

            holder.ivTag.setOnClickListener {
                onListener.onAddTagComment(model, holder.lineTag)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var lineHeader: LinearLayout
        var lineCommentLeft: RelativeLayout
        var lineCommentRight: RelativeLayout
        var imgAvatarLeft: ImageView
        var imgAvatarRight: ImageView
        var tvCommentLeft: TextView
        var tvCommentRight: TextView
        var tvUserNameLeft: TextView
        var tvUserNameRight: TextView
        var tvTimeLeft: TextView
        var tvTimeRight: TextView
        var tvTotalReplyLeft: TextView
        var tvTotalReplyRight: TextView
        var tvTotalUserRight: TextView
        var tvTotalUserLeft: TextView
        var imgUserLeft: ImageView
        var imgUser1Left: ImageView
        var imgUserRight: ImageView
        var imgUser1Right: ImageView
        var flTotalUserRight: FrameLayout
        var flTotalUserLeft: FrameLayout
        var lineReply: LinearLayout
        var ivTag: ImageView
        var icRemoveTag: ImageView
        var tagName: TextView
        var lineTag: LinearLayout
        var ivDeleteComment: ImageView
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
            lineHeader = itemView.findViewById(R.id.lineHeader)
            lineCommentLeft = itemView.findViewById(R.id.lineCommentLeft)
            lineCommentRight = itemView.findViewById(R.id.lineCommentRight)
            imgAvatarLeft = itemView.findViewById(R.id.imgAvatarLeft)
            imgAvatarRight = itemView.findViewById(R.id.imgAvatarRight)
            tvCommentLeft = itemView.findViewById(R.id.tvCommentLeft)
            tvCommentRight = itemView.findViewById(R.id.tvCommentRight)
            tvUserNameLeft = itemView.findViewById(R.id.tvUserNameLeft)
            tvUserNameRight = itemView.findViewById(R.id.tvUserNameRight)
            tvTimeLeft = itemView.findViewById(R.id.tvTimeLeft)
            tvTimeRight = itemView.findViewById(R.id.tvTimeRight)
            tvTotalReplyLeft = itemView.findViewById(R.id.tvTotalReplyLeft)
            tvTotalReplyRight = itemView.findViewById(R.id.tvTotalReplyRight)
            imgUserLeft = itemView.findViewById(R.id.imgUserLeft)
            imgUser1Left = itemView.findViewById(R.id.imgUser1Left)
            imgUserRight = itemView.findViewById(R.id.imgUserRight)
            imgUser1Right = itemView.findViewById(R.id.imgUser1Right)
            flTotalUserRight = itemView.findViewById(R.id.flTotalUserRight)
            flTotalUserLeft = itemView.findViewById(R.id.flTotalUserLeft)
            tvTotalUserRight = itemView.findViewById(R.id.tvTotalUserRight)
            tvTotalUserLeft = itemView.findViewById(R.id.tvTotalUserLeft)
            lineReply = itemView.findViewById(R.id.lineReply)
            ivTag = itemView.findViewById(R.id.ivTag)
            tagName = itemView.findViewById(R.id.tagName)
            lineTag = itemView.findViewById(R.id.lineTag)
            icRemoveTag = itemView.findViewById(R.id.icRemoveTag)
            ivDeleteComment = itemView.findViewById(R.id.ivDeleteComment)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return commentModel.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}