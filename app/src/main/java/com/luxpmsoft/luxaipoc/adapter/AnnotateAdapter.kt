package com.luxpmsoft.luxaipoc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel
import com.luxpmsoft.luxaipoc.widget.DialogFactory

class AnnotateAdapter(
    private val context: Activity,
    private val imagePaths: ArrayList<ImageFrameModel>,
    val resource: Int,
    onListener: OnListener
) : RecyclerView.Adapter<AnnotateAdapter.ImageHolder>() {
    val onListener: OnListener = onListener
    var isSelect = false
    var isDelete = false
    var doubleBackToExitPressedOnce = false

    interface OnListener {
        fun onDeleteListener(position: Int, isSelect: Boolean)
        fun onSelectListener(position: Int, isSelect: Boolean)
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

        val displayMetrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        holder.imageView.layoutParams.width = width / 3
        holder.imageView.layoutParams.height = width / 3
        holder.lineCheck.layoutParams.width = width / 3
        holder.lineCheck.layoutParams.height = width / 3
        holder.lineDelete.layoutParams.width = width / 3
        holder.lineDelete.layoutParams.height = width / 3
        holder.lineCheck.visibility = View.GONE
        holder.lineDelete.visibility = View.GONE
        if (isSelect) {
            holder.lineCheck.visibility = View.VISIBLE
            holder.lineDelete.visibility = View.GONE
        }

        if (isDelete) {
            holder.lineDelete.visibility = View.VISIBLE
            holder.lineCheck.visibility = View.GONE
        }

        holder.ckbSelect.isChecked = false
        imagePaths[position].isCheck?.let {
            holder.ckbSelect.isChecked = it
        }

        holder.ckbDelete.isChecked = false
        imagePaths[position].isDelete?.let {
            holder.ckbDelete.isChecked = it
        }

        holder.ckbSelect.setOnClickListener {
            onListener.onSelectListener(position, holder.ckbSelect.isChecked)
        }
        holder.ckbDelete.setOnClickListener {
            onListener.onDeleteListener(position, holder.ckbDelete.isChecked)
        }

        holder.imageView.setOnClickListener {
            if (doubleBackToExitPressedOnce) {
                return@setOnClickListener
            }

            doubleBackToExitPressedOnce = true

            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 1200)

            if (!isSelect && !isDelete) {
                DialogFactory.dialogShowImage(context, imagePaths, "", position)
            }
        }
    }

    override fun getItemCount(): Int {
        return imagePaths.size
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: AppCompatImageView = itemView.findViewById(R.id.image)
        var lineCheck: LinearLayoutCompat = itemView.findViewById(R.id.lineCheck)
        var lineDelete: LinearLayoutCompat = itemView.findViewById(R.id.lineDelete)
        var ckbSelect: CheckBox = itemView.findViewById(R.id.ckbSelect)
        var ckbDelete: CheckBox = itemView.findViewById(R.id.ckbDelete)
    }
}