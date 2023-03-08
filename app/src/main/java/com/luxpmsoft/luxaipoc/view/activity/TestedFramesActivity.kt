package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.AnnotateAdapter
import com.luxpmsoft.luxaipoc.adapter.SortAdapter
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.model.SortModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFramesList
import com.luxpmsoft.luxaipoc.model.defect_detect.TestedModelsData
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModels
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_tested_frames.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TestedFramesActivity: BaseActivity(), AnnotateAdapter.OnListener {
    private var imagePaths = ArrayList<ImageFrameModel>()
    val imageDefect = ArrayList<ImageFrameModel>()
    private lateinit var annotateAdapter: AnnotateAdapter
    var mFolderName = "detect_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    var mDirectory: File? = null
    var frames: TestedModelsData? = null
    var trainedModels: TrainedModels? = null
    var index = 0
    var adapter: SortAdapter? = null
    var sortList:ArrayList<SortModel> = ArrayList()
    var type = "ALL"
    var doublePressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tested_frames)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        mDirectory = MyUtils.getOutputDefectDirectory(this, mFolderName)
        Utils.gridLayoutManager(this, grvFrames, 3, GridLayoutManager.VERTICAL)
        annotateAdapter = AnnotateAdapter(this, imagePaths, R.layout.item_annotate, this)
        grvFrames.adapter = annotateAdapter
        val bundle = intent.extras
        if (bundle != null) {
            trainedModels = Gson().fromJson(bundle.get("trained").toString(), TrainedModels::class.java)
            frames = Gson().fromJson(bundle.get("frames").toString(), TestedModelsData::class.java)
            frames?.image_paths?.defect_paths_no_bb?.addAll(frames?.image_paths?.nondefect_paths!!)
            frames?.image_paths?.paths?.let {
                MyUtils.showProgress(this, flProgress)
                for (f in it) {
                    imagePaths.add(ImageFrameModel(BuildConfig.URL_IMAGE+f, false))
                }
                MyUtils.hideProgress(this, flProgress)
            }

            sortList.add(SortModel("ALL", true, "All"))
            sortList.add(SortModel("DEFECTED", false, "Defected"))
            sortList.add(SortModel("NOT_DEFECTED", false,"Not Defected"))
        }

    }

    fun listener() {
        icBack.setOnClickListener {
            finish()
        }
        btnContinue.setOnClickListener {
            if (doublePressedOnce) {
                return@setOnClickListener
            }
            doublePressedOnce = true
            Handler().postDelayed(Runnable { doublePressedOnce = false }, 1200)

            if (frames?.image_paths?.defect_paths_no_bb != null) {
                saveImage(frames?.image_paths?.defect_paths_no_bb)
            } else {
                saveImage(frames?.image_paths?.nondefect_paths)
            }
        }

        lnFilter.setOnClickListener {
            showPopupFilter(lnFilter)
        }
    }

    fun saveImage(image: ArrayList<String>?) {
        MyUtils.showProgress(this, flProgress)
        imageDefect.clear()
        saveDefectPathsNoBb(image)
    }

    fun saveDefectPathsNoBb(image: ArrayList<String>?) {
        index = 0
        var sizePath = imagePaths.size
        if (type == "ALL") {
            if (frames?.image_paths?.defect_paths_no_bb == null) {
                sizePath -= frames?.image_paths?.defect_paths?.size!!
            }
        }
        image?.let {
            if (it.isNotEmpty()) {
                for (origin in it) {
                    val fileOriginName: String = origin.substring(origin.lastIndexOf("/"), origin.length)
                    for (file in imagePaths) {
                        val fileName: String = file.uri!!.substring(file.uri!!.lastIndexOf("/"), file.uri!!.length)
                        if (fileOriginName.replace("_output.jpg",".jpeg").replace(".jpg", ".jpeg")
                            == fileName.replace("_output.jpg",".jpeg")) {
                            Glide.with(this)
                                .asBitmap()
                                .load(BuildConfig.URL_IMAGE+origin)
                                .into(object : CustomTarget<Bitmap>(){
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        val fileName: String = origin.substring(origin.lastIndexOf("/"), origin.length).replace("_output.jpg",".jpeg")
                                        val frameFile = File(mDirectory, fileName)
                                        MyUtils.saveImageToFile(resource, frameFile)
                                        imageDefect.add(ImageFrameModel(frameFile.path, false))
                                        index += 1
                                        if (index == sizePath) {
                                            startDefectDetect(imageDefect)
                                        }
                                    }
                                    override fun onLoadCleared(placeholder: Drawable?) {
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        super.onLoadFailed(errorDrawable)
                                        index += 1
                                        if (index == sizePath) {
                                            startDefectDetect(imageDefect)
                                        }
                                    }
                                })
                        }
                    }

                }
            }
        }
    }

    fun startDefectDetect(paths: ArrayList<ImageFrameModel>) {
        val imageList = ImageFramesList(paths)
        val intent = Intent(this, DefectDetectActivity::class.java)
        intent.putExtra("image", Gson().toJson(imageList))
        intent.putExtra("folderZip", "NO")
        intent.putExtra("path", mDirectory?.path)
        intent.putExtra("trained", Gson().toJson(trainedModels))
        startActivity(intent)
        index = 0
        MyUtils.hideProgress(this, flProgress)
    }

    override fun onDeleteListener(position: Int, isSelect: Boolean) {

    }

    override fun onSelectListener(position: Int, isSelect: Boolean) {

    }

    private fun showPopupFilter(anchor: View?) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.popup_sort_recent_model, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }

            val sortRecycler = contentView.findViewById<RecyclerView>(R.id.grvSort)
            Utils.gridLayoutManager(this@TestedFramesActivity, sortRecycler, 1, GridLayoutManager.VERTICAL)
            adapter = SortAdapter(this@TestedFramesActivity, R.layout.item_sort, sortList, object :
                SortAdapter.IAdapterClickListener {
                override fun onSelect(position: Int, isCheck: Boolean) {
                    try {
                        if (type != sortList.get(position).name) {
                            sortList.forEachIndexed { index, element ->
                                if (index == position) {
                                    sortList.get(index)?.isCheck =
                                        sortList.get(index)?.isCheck != true
                                } else {
                                    sortList.get(index).isCheck = false
                                }
                            }

                            val nameSort = sortList.get(position).name
                            type = nameSort!!
                            imagePaths.clear()
                            if (nameSort == "ALL") {
                                frames?.image_paths?.paths?.let {
                                    for (f in it) {
                                        imagePaths.add(
                                            ImageFrameModel(
                                                BuildConfig.URL_IMAGE + f,
                                                false
                                            )
                                        )
                                    }
                                }
                            } else if (nameSort == "DEFECTED")  {
                                frames?.image_paths?.defect_paths?.let {
                                    for (f in it) {
                                        imagePaths.add(
                                            ImageFrameModel(
                                                BuildConfig.URL_IMAGE + f,
                                                false
                                            )
                                        )
                                    }
                                }
                                tvEmpty.text = resources.getText(R.string.str_no_defect)
                            } else {
                                frames?.image_paths?.nondefect_paths?.let {
                                    for (f in it) {
                                        imagePaths.add(
                                            ImageFrameModel(
                                                BuildConfig.URL_IMAGE + f,
                                                false
                                            )
                                        )
                                    }
                                }
                                tvEmpty.text = resources.getText(R.string.str_no_not_defect)
                            }

                            adapter?.notifyDataSetChanged()
                            annotateAdapter.notifyDataSetChanged()
                            if (imagePaths.isNotEmpty()) {
                                tvEmpty.visibility = View.GONE
                                btnContinue.visibility = View.VISIBLE
                                //check defect path no bb empty
                                if (type == "DEFECTED" && frames?.image_paths?.defect_paths_no_bb == null) {
                                    Toast.makeText(this@TestedFramesActivity, resources.getString(R.string.str_nodefect_paths_no_bb), Toast.LENGTH_SHORT).show()
                                    btnContinue.visibility = View.GONE
                                }
                            } else {
                                tvEmpty.visibility = View.VISIBLE
                                btnContinue.visibility = View.GONE
                            }
                            dismiss()
                        }

                    } catch (e:Exception) {
                        e.message
                    }
                }
            })
            sortRecycler.adapter = adapter
        }.also { popupWindow ->
            popupWindow.height = LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            popupWindow.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT)
            )

            // Absolute location of the anchor view
            val location = IntArray(2).apply {
                anchor?.getLocationOnScreen(this)
            }
            val size = Size(
                popupWindow.contentView.measuredWidth,
                popupWindow.contentView.measuredHeight
            )
            popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.END,
                resources.getDimensionPixelOffset(R.dimen.size_26),
                location[1] - size.height+resources.getDimensionPixelOffset(R.dimen.size_26)+popupWindow.contentView.measuredHeight
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imagePaths.clear()
        imageDefect.clear()
        sortList.clear()
    }
}