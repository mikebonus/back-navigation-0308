package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.recentmodel.CadFileRows
import com.luxpmsoft.luxaipoc.utils.MyUtils
import de.hdodenhof.circleimageview.CircleImageView


class BoardAdapter (
    activity: Activity,
    resource: Int,
    boardModel: ArrayList<CadFileRows>,
    onListener: OnListener
) :
    RecyclerView.Adapter<BoardAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val boardModel: ArrayList<CadFileRows> = boardModel
    var isRemove = false
    var isEdit = false
    var selectedPosition = -1
    var lastPosition = -1

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onBoardListener(model: String)
        fun onMore(model: String, view: View?, position: Int)
        fun onSelect(position: Int, isCheck: Boolean)
        fun onEdit(position: Int, lastPosition:Int, isCheck: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = boardModel.get(position)
        try {
            if(model.name != null) {
                holder.tvFileName.text = model.name
            }

            model.createdAt?.let {
                holder.tvTime.text = MyUtils.convertDateTime(it)
            }

            model.description?.let {
                holder.tvDescription.text = it
            }

            holder.ckbChoose.visibility = View.GONE
            if (isRemove) {
                holder.ckbChoose.visibility = View.VISIBLE
            }

            holder.ckbChoose.isChecked = false
            model.isRemove?.let {
                if (it) {
                    holder.ckbChoose.isChecked = true
                }
            }

            holder.rdEdit.visibility = View.GONE
            if (isEdit) {
                holder.rdEdit.visibility = View.VISIBLE
            }

            holder.rdEdit.isChecked = false
            model.isEdit?.let {
                if (it) {
                    holder.rdEdit.isChecked = true
                }
            }

            model.reconstructions?.let {
                try {
                    it[0]?.thumbnailImageKey?.let {
                        MyUtils.loadReconstructionImage(activity!!, (activity?.application as LidarApp).prefManager?.getToken(),
                            it, holder.iv_image, activity!!.resources.getDrawable(R.drawable.ic_empty))
                    }
                } catch (e: ArrayIndexOutOfBoundsException) {
                }
            }

            model.totalComment?.let {
                holder.tvTotalComment.text = it.toString().plus(" "  + activity!!.resources.getString(R.string.comments))
            }

            model.tags?.let {
                if (it.isNotEmpty()) {
                    if (it.size == 1) {
                        it[0].user?.profileImageKey?.let {
                            MyUtils.loadAvatar(activity!!, (activity?.application as LidarApp).prefManager?.getToken(), it,
                                holder.imgAvatar, activity!!.resources.getDrawable(R.drawable.user))
                        }

                        holder.imgAvatar.visibility = View.VISIBLE
                    } else if (it.size == 2) {
                        it[1].user?.profileImageKey?.let {
                            MyUtils.loadAvatar(activity!!, (activity?.application as LidarApp).prefManager?.getToken(), it,
                                holder.imgAvatar1, activity!!.resources.getDrawable(R.drawable.user))
                        }
                        holder.imgAvatar1.visibility = View.VISIBLE
                    } else {
                        it[0].user?.profileImageKey?.let {
                            MyUtils.loadAvatar(activity!!, (activity?.application as LidarApp).prefManager?.getToken(), it,
                                holder.imgAvatar, activity!!.resources.getDrawable(R.drawable.user))
                        }

                        holder.imgAvatar.visibility = View.VISIBLE

                        it[1].user?.profileImageKey?.let {
                            MyUtils.loadAvatar(activity!!, (activity?.application as LidarApp).prefManager?.getToken(), it,
                                holder.imgAvatar1, activity!!.resources.getDrawable(R.drawable.user))
                        }
                        holder.imgAvatar1.visibility = View.VISIBLE

                        holder.tvTotalUser.text = "+".plus(it.size-2)
                        holder.flTotalUser.visibility = View.VISIBLE
                    }
                }
            }

            holder.icMore.setOnClickListener {
                model.cadFileID?.let { onListener.onMore(it, holder.icMore, position) }
            }

            // Checked selected radio button
            holder.rdEdit.setChecked(position === selectedPosition)

//            // set listener on radio button
//            holder.rdEdit.setOnCheckedChangeListener(
//                CompoundButton.OnCheckedChangeListener { compoundButton, b ->
//                    // check condition
//                    lastPosition = selectedPosition
//                    if (b) {
//                        // When checked
//                        // update selected position
//                        selectedPosition = holder.adapterPosition
//                        // Call listener
//                    }
//                })

            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    if (isRemove) {
                        onListener.onSelect(position, !holder.ckbChoose.isChecked)
                    } else if(isEdit) {
                        selectedPosition = holder.adapterPosition
                        onListener.onEdit(position, lastPosition, !holder.rdEdit.isChecked)
                    }
                    else {
                        model.cadFileID?.let { onListener.onBoardListener(it) }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var tvFileName: TextView
        var tvTime: TextView
        var tvTotalComment: TextView
        var tvDescription: TextView
        var iv_image: ImageView
        var tvTotalUser: TextView
        var imgAvatar: CircleImageView
        var imgAvatar1: CircleImageView
        var flTotalUser: FrameLayout
        var icMore: ImageView
        var ckbChoose: CheckBox
        var rdEdit: RadioButton
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
            tvFileName = itemView.findViewById(R.id.tvFileName)
            tvTime = itemView.findViewById(R.id.tvTime)
            tvTotalComment = itemView.findViewById(R.id.tvTotalComment)
            tvTotalUser = itemView.findViewById(R.id.tvTotalUser)
            iv_image = itemView.findViewById(R.id.iv_image)
            imgAvatar = itemView.findViewById(R.id.imgAvatar)
            imgAvatar1 = itemView.findViewById(R.id.imgAvatar1)
            flTotalUser = itemView.findViewById(R.id.flTotalUser)
            icMore = itemView.findViewById(R.id.icMore)
            ckbChoose = itemView.findViewById(R.id.ckbChoose)
            rdEdit = itemView.findViewById(R.id.rdEdit)
            tvDescription = itemView.findViewById(R.id.tvDescription)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return boardModel.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}