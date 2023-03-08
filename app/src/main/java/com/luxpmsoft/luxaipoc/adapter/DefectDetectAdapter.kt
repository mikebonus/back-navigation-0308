package com.luxpmsoft.luxaipoc.adapter

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel

class DefectDetectAdapter(
    private val context: Context,
    private val imagePaths: ArrayList<ImageFrameModel>,
    val resource: Int,
    onListener: OnListener
) : RecyclerView.Adapter<DefectDetectAdapter.ImageHolder>() {
    val onListener: OnListener = onListener
    private var imageSize = 0
    private fun setColumnNumber(context: Context, columnNum: Int) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        val widthPixels = metrics.widthPixels
        imageSize = widthPixels / columnNum
    }

    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onCheckListener(position: Int)
    }

    init {
        setColumnNumber(context, 3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(resource, parent, false)
        return ImageHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
//        holder.title.text = imageTitles[position]

        Glide.with(context)
            .load(imagePaths[position].uri)
            .apply(
                RequestOptions.centerCropTransform()
                    .dontAnimate()
                    .override(imageSize, imageSize)
            )
            .thumbnail(0.5f)
            .into(holder.imageView)

        if (imagePaths[position].isCheck == true) {
            holder.lineDelete.visibility = View.VISIBLE
        } else {
            holder.lineDelete.visibility = View.GONE
        }

        holder.setItemClickListener(object : ItemClickListener {
            override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                onListener.onCheckListener(position)
            }
        })
    }

    override fun getItemCount(): Int {
        return imagePaths.size
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var imageView: AppCompatImageView = itemView.findViewById(R.id.image)
        var lineDelete: LinearLayoutCompat = itemView.findViewById(R.id.lineDelete)

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
            itemView.setOnClickListener(this)
        }
    }
}