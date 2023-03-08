package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.DefectDetectAdapter
import com.luxpmsoft.luxaipoc.adapter.ImageAdapter
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFramesList
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModels
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_defect_detect.*
import java.io.File
import java.util.ArrayList

class DefectDetectActivity: AppCompatActivity(), ImageAdapter.OnListener, DefectDetectAdapter.OnListener {
    private var image: ImageFramesList? = null
    private var folderZipPath = ""
    private var path = ""
    private lateinit var defectDetectAdapter: DefectDetectAdapter
    private lateinit var imageLargeAdapter: ImageAdapter
    private var imagePaths = ArrayList<ImageFrameModel>()
    var pos = 0
    var trainedModels: TrainedModels? = null
    var doublePressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_defect_detect)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            folderZipPath = bundle.get("folderZip").toString()
            path = bundle.get("path").toString()
            image = Gson().fromJson(bundle.get("image").toString(), ImageFramesList::class.java)
            image?.image?.let {
                imagePaths = it
            }

            trainedModels = Gson().fromJson(bundle.get("trained").toString(), TrainedModels::class.java)
        }

        runOnUiThread {
            Utils.gridLayoutManager(this, rcvImageFrames, 1, GridLayoutManager.HORIZONTAL)
            Utils.gridLayoutManager(this, rcvImageFramesLarge, 1, GridLayoutManager.HORIZONTAL)
            imagePaths?.let {
                if (imagePaths.isNotEmpty()) {
                    imagePaths.get(0).isCheck = true
                }
            }
            defectDetectAdapter = DefectDetectAdapter(this, imagePaths, R.layout.item_image_defect_detect, this)
            rcvImageFrames.adapter = defectDetectAdapter
            imageLargeAdapter = ImageAdapter(this, imagePaths, R.layout.item_image_frame_large, this)
            rcvImageFramesLarge.adapter = imageLargeAdapter
        }
    }

    fun listener() {
        rcvImageFramesLarge.setOnTouchListener({ v, event -> true })

        icBack.setOnClickListener {
            finish()
        }

        scrollLeft.setOnClickListener {
            scrollLeft()
        }

        scrollRight.setOnClickListener {
            scrollRight()
        }

        icScrollLeft.setOnClickListener {
            scrollLeft()
        }

        icScrollRight.setOnClickListener {
            scrollRight()
        }

        btnAddRetrain.setOnClickListener {
            if (doublePressedOnce) {
                return@setOnClickListener
            }
            doublePressedOnce = true
            Handler().postDelayed(Runnable { doublePressedOnce = false }, 1200)

            val list: ArrayList<ImageFrameModel>  = ArrayList()
            for (item in imagePaths) {
                if (item.isCheck == true) {
                    val data = ImageFrameModel(item.uri)
                    list.add(data)
                }
            }
            if (list.isNotEmpty()) {
                val intent = Intent(this, AnnotateActivity::class.java)
                intent.putExtra("image", Gson().toJson(ImageFramesList(list)))
                intent.putExtra("path", path)
                intent.putExtra("trained", Gson().toJson(trainedModels))
                startActivity(intent)
            } else {
                Toast.makeText(this, resources.getString(R.string.str_choose_image), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun scrollLeft() {
        if (pos > 0) {
            imagePaths[pos].isCheck = false
            pos -= 1
            rcvImageFramesLarge.scrollToPosition(pos)
            rcvImageFrames.scrollToPosition(pos)

            if (pos == 0) {
                scrollLeft.visibility = View.GONE
            }
            if (scrollRight.visibility == View.GONE) {
                scrollRight.visibility = View.VISIBLE
            }
            imagePaths[pos].isCheck = true
            defectDetectAdapter.notifyDataSetChanged()
        }
    }

    fun scrollRight() {
        if (pos < imagePaths.size-1) {
            if (scrollLeft.visibility == View.GONE) {
                scrollLeft.visibility = View.VISIBLE
            }
            imagePaths[pos].isCheck = false
            pos += 1

            rcvImageFramesLarge.scrollToPosition(pos)
            rcvImageFrames.scrollToPosition(pos)
            if (pos == imagePaths.size-1) {
                scrollRight.visibility = View.GONE
            }

            imagePaths[pos].isCheck = true
            defectDetectAdapter.notifyDataSetChanged()
        }
    }

    override fun onCheckListener(position: Int) {
        imagePaths.get(position).isCheck = imagePaths.get(position).isCheck != true
        pos = position
        rcvImageFramesLarge.scrollToPosition(pos)
        defectDetectAdapter.notifyDataSetChanged()
        if (pos <= 0) {
            scrollLeft.visibility = View.GONE
        } else {
            scrollLeft.visibility = View.VISIBLE
        }

        if (pos >= imagePaths.size -1) {
            scrollRight.visibility = View.GONE
        } else {
            scrollRight.visibility = View.VISIBLE
        }
    }

    override fun onDeleteListener(posi: Int ,position: ImageFrameModel) {

    }

    override fun onItemListener(position: Int) {

    }

    override fun onDestroy() {
        super.onDestroy()
        image = null
        imagePaths.clear()
        removeFile()
    }

    fun removeFile() {
        path?.let {
            File(path)?.let {
                it.listFiles()?.let {
                    for (f in it) {
                        f.delete()
                    }
                }
            }
        }
    }
}