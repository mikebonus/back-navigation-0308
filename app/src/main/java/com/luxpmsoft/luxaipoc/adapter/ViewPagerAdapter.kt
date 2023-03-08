package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.defect_detect.IDrawListener
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel
import com.luxpmsoft.luxaipoc.model.workout.Workout
import com.luxpmsoft.luxaipoc.utils.MyUtils.Companion.setLayoutSize
import com.luxpmsoft.luxaipoc.widget.DrawImageView
import java.io.File
import java.io.FileOutputStream

class ViewPagerAdapter(private val context: Activity, val imagePaths: ArrayList<ImageFrameModel>, val path: String, val isAction: Boolean = false,
                       onListener: OnListener) :
    PagerAdapter(), IDrawListener {
    private var layoutInflater: LayoutInflater? = null
    var currentItem: View? = null
    var pos = -1
    val onListener: OnListener = onListener
    // Saving current active item
    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        pos = position
        currentItem = `object` as View
    }

    override fun getCount(): Int {
        return imagePaths.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater!!.inflate(R.layout.item_retrain, container, false)
        val imageView = view.findViewById<DrawImageView>(R.id.image)
        val lineDelete = view.findViewById<LinearLayout>(R.id.lineDelete)
        val lineZoom = view.findViewById<LinearLayout>(R.id.lineZoom)
        val lineMove = view.findViewById<LinearLayout>(R.id.lineMove)
        val lineAction = view.findViewById<LinearLayoutCompat>(R.id.lineAction)
        imageView.setOnDrawListener(this)
        lineAction.visibility = View.VISIBLE

        if (isAction) {
            lineAction.visibility = View.GONE
            imageView.isMove = true
        } else {
            imageView.sessionInfoFile =
                File(File(path), File(imagePaths[position].uri.toString()).name.replace(".jpeg", "")+".txt")
            if (imageView.sessionInfoFile?.exists() != true) {
                imageView.sessionInfoFile?.createNewFile()
            }
            imageView.fosSessionInfoData = FileOutputStream(imageView.sessionInfoFile, true)
        }

        Glide.with(context)
            .asBitmap()
            .load(imagePaths[position].uri)
//            .apply(
//                RequestOptions.centerCropTransform()
//                    .dontAnimate()
//            )
            .thumbnail(0.5f)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)
                    imageView.mBitmap = resource
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    // this is called when imageView is cleared on lifecycle call or for
                    // some other reason.
                    // if you are referencing the bitmap somewhere else too other than this imageView
                    // clear it here as you can no longer have the bitmap
                }
            })
//        Glide.with(context)
//            .load(imagePaths[position].uri)
//            .apply(
//                RequestOptions.centerCropTransform()
//                    .dontAnimate()
//            )
//            .thumbnail(0.5f)
//            .into(imageView)

        lineDelete.setOnClickListener {
            pos = position

            imageView.mCircles?.let {
                imageView.circleZoom = false
                imageView.isMove = false
                imageView.isDraw = false
                imageView.mCircles.clear()
                imageView.mCirclePointer.clear()
                imageView.invalidate()
                setLayoutSize(lineZoom,false)
                setLayoutSize(lineMove, false)
                onListener.onDrawListener(pos, false)
            }
        }

        lineZoom.setOnClickListener {
            pos = position

            imageView.circleZoom?.let {
                imageView.circleZoom = !it
                imageView.isMove = false
                setLayoutSize(lineZoom, imageView.circleZoom)
                setLayoutSize(lineMove, false)
                imageView.invalidate()
            }
        }

        lineMove.setOnClickListener {
            pos = position

            imageView.isMove?.let {
                imageView.invalidate()
                imageView.isMove = !it
                imageView.circleZoom = false
                setLayoutSize(lineMove, imageView.isMove)
                setLayoutSize(lineZoom, false)
            }
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }

    interface OnListener {
        fun onDrawListener(position: Int, isDraw: Boolean?)
    }

    override fun onDrawListener(isDraw: Boolean?) {
        onListener.onDrawListener(pos, isDraw)
    }
}