package com.luxpmsoft.luxaipoc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.repositories.RepositoriesModel
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.PrefManager
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class RepositoriesAdapter (
    activity: Activity,
    resource: Int,
    repositoriesModel: ArrayList<RepositoriesModel>,
    onListener: OnListener
) :
    RecyclerView.Adapter<RepositoriesAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val repositoriesModel: ArrayList<RepositoriesModel> = repositoriesModel
    var prefManager: PrefManager? = PrefManager(activity)

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onListener(repositoryId: String, repositoryName: String, organizationId: String)
        fun onMore(repositoryId: String, repositoryName: String, view: View?, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = repositoriesModel.get(position)
        try {
            if(model.repositoryName != null) {
                holder.tvName.text = model.repositoryName
            }

            if (model.isPublic == true) {
                holder.tvAccess.text = activity!!.resources.getString(R.string.public_repo)
                MyUtils.loadImage(activity!!, prefManager?.getToken()!!, model.isPublic.toString(), holder.ivAccess,
                activity!!.resources.getDrawable(R.drawable.ic_public))
            } else {
                holder.tvAccess.text = activity!!.resources.getString(R.string.private_repo)
                MyUtils.loadImage(activity!!, prefManager?.getToken()!!, model.isPublic.toString(), holder.ivAccess,
                    activity!!.resources.getDrawable(R.drawable.ic_private))
            }

            try {
                model.repositoryPhoto?.let {
                    MyUtils.loadImage(activity!!, prefManager?.getToken()!!, model.repositoryPhoto, holder.iv_image,
                        activity!!.resources.getDrawable(R.drawable.ic_empty))
                }
            } catch (e: Exception) {
                e.message
            }

            model.repositoryusers?.let {
                if (it.isNotEmpty()) {
                    it[0]?.created_at?.let {
                        holder.tvTime.text = MyUtils.convertDateTime(it)
                    }
                    if (it.size == 1) {
                        it[0].organizationuser?.user?.profileImageKey?.let {
                            MyUtils.loadAvatar(activity!!, prefManager?.getToken(), it,
                                holder.imgAvatar, activity!!.resources.getDrawable(R.drawable.user))
                        }

                        holder.imgAvatar.visibility = View.VISIBLE
                    } else {
                        it[0].organizationuser?.user?.profileImageKey?.let {
                            MyUtils.loadAvatar(activity!!, prefManager?.getToken(), it,
                                holder.imgAvatar, activity!!.resources.getDrawable(R.drawable.user))
                        }

                        holder.imgAvatar.visibility = View.VISIBLE

                        it[1].organizationuser?.user?.profileImageKey?.let {
                            MyUtils.loadAvatar(activity!!, prefManager?.getToken(), it,
                                holder.imgAvatar1, activity!!.resources.getDrawable(R.drawable.user))
                        }
                        holder.imgAvatar1.visibility = View.VISIBLE

                        if (it.size > 2) {
                            holder.tvTotalUser.text = "+".plus(it.size-2)
                            holder.flTotalUser.visibility = View.VISIBLE
                        }
                    }
                }
            }

            holder.icMore.setOnClickListener {
                model.repositoryId?.let {
                    onListener.onMore(it, model.repositoryName!!, holder.icMore, position)
                }
            }
            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    model.repositoryId?.let { onListener.onListener(it, model.repositoryName!!, model.organizationId!!) }
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
        var tvTotalUser: TextView
        var flTotalUser: FrameLayout
        var iv_image: ImageView
        var icMore: LinearLayout
        var imgAvatar: CircleImageView
        var imgAvatar1: CircleImageView
        var ivAccess: ImageView
        var tvAccess: TextView
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
            tvTime = itemView.findViewById(R.id.tvTime)
            tvTotalUser = itemView.findViewById(R.id.tvTotalUser)
            iv_image = itemView.findViewById(R.id.iv_image)
            icMore = itemView.findViewById(R.id.icMore)
            imgAvatar = itemView.findViewById(R.id.imgAvatar)
            imgAvatar1 = itemView.findViewById(R.id.imgAvatar1)
            flTotalUser = itemView.findViewById(R.id.flTotalUser)
            ivAccess = itemView.findViewById(R.id.ivAccess)
            tvAccess = itemView.findViewById(R.id.tvAccess)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return repositoriesModel.size
    }
}