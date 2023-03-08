package com.luxpmsoft.luxaipoc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel

class ImageAdapter(
    private val context: Activity,
    private val imagePaths: ArrayList<ImageFrameModel>,
    val resource: Int,
    onListener: OnListener,
    val isDelete: Boolean = false,
    val isBackGround: Boolean = false
) : RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    val onListener: OnListener = onListener
    var doubleBackToExitPressedOnce = false

    private var imageSize = 0
//    private fun setColumnNumber(context: Context, columnNum: Int) {
//        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        val metrics = DisplayMetrics()
//        wm.defaultDisplay.getMetrics(metrics)
//        val widthPixels = metrics.widthPixels
//        imageSize = widthPixels / columnNum
//    }

    interface OnListener {
        fun onDeleteListener(posi: Int ,position: ImageFrameModel)
        fun onItemListener(position: Int)
    }

    init {
//        setColumnNumber(context, 3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(resource, parent, false)
        return ImageHolder(itemView)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
//        holder.title.text = imageTitles[position]
        Glide.with(context)
            .load(imagePaths[position].uri)
//            .apply(
//                RequestOptions.centerCropTransform()
//                    .dontAnimate()
////                    .override(imageSize, imageSize)
//            )
            .thumbnail(0.5f)
            .into(holder.imageView)

        if (isBackGround) {
            if (imagePaths[position].isCheck == true) {
                holder.viewRoot.background = context.resources.getDrawable(R.drawable.bg_transparent_stroke_red_10)
            } else {
                holder.viewRoot.background = null
            }

            if (imagePaths[position].isAnnotate == true) {
                holder.lineDelete.visibility = View.VISIBLE
            } else {
                holder.lineDelete.visibility = View.GONE
            }
        }

        if (isDelete) {
            if (imagePaths[position].isCheck == true) {
                holder.lineDelete.visibility = View.VISIBLE
            } else {
                holder.lineDelete.visibility = View.GONE
            }
        }

        holder.lineDelete.setOnClickListener {
            if (doubleBackToExitPressedOnce) {
                return@setOnClickListener
            }

            doubleBackToExitPressedOnce = true

            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 1100)
            onListener.onDeleteListener(position, imagePaths[position])
        }

        holder.viewRoot.setOnClickListener {
            onListener.onItemListener(position)
        }
    }

    override fun getItemCount(): Int {
        return imagePaths.size
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: AppCompatImageView = itemView.findViewById(R.id.image)
        var lineDelete: LinearLayoutCompat = itemView.findViewById(R.id.lineDelete)
        var viewRoot: FrameLayout = itemView.findViewById(R.id.view_root)
    }
}