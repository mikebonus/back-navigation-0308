package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.AnnotateAdapter
import com.luxpmsoft.luxaipoc.adapter.ImageAdapter
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFramesList
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModels
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import com.luxpmsoft.luxaipoc.widget.TextViewFonts
import kotlinx.android.synthetic.main.activity_annotate.*
import java.io.File

class AnnotateActivity: BaseActivity(), AnnotateAdapter.OnListener {
    private var image: ImageFramesList? = null
    private lateinit var annotateAdapter: AnnotateAdapter
    var trainedModels: TrainedModels? = null
    private var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        setContentView(R.layout.activity_annotate)
        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            image = Gson().fromJson(bundle.get("image").toString(), ImageFramesList::class.java)
            path = bundle.get("path").toString()
            trainedModels = Gson().fromJson(bundle.get("trained").toString(), TrainedModels::class.java)
        }

        Utils.gridLayoutManager(this, rcvAnnotate, 3, GridLayoutManager.VERTICAL)
        annotateAdapter = AnnotateAdapter(this, image!!.image!!, R.layout.item_annotate, this)
        rcvAnnotate.adapter = annotateAdapter
    }

    fun listener() {
        btnNext.setOnClickListener {
            var listImage: ArrayList<ImageFrameModel> =  ArrayList()
            image?.image?.forEachIndexed { index, element ->
                if (element.isCheck == true) {
                    listImage.add(ImageFrameModel(element.uri))
                }
            }

            val image= ImageFramesList(listImage)
            val intent = Intent(this, RetrainDefectDetectActivity::class.java)
            intent.putExtra("image", Gson().toJson(image))
            intent.putExtra("path", path)
            intent.putExtra("trained", Gson().toJson(trainedModels))
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            var listImage: ArrayList<ImageFrameModel> =  ArrayList()
            var index1 = 0
            image?.image?.forEachIndexed { index, element ->
                if (element.isDelete == true) {
                    index1 += 1
                }
            }
            DialogFactory.dialogDelete(
                this,
                object : DialogFactory.Companion.DialogListener.Delete {
                    override fun delete() {
                        image?.image?.forEachIndexed { index, element ->
                            if (element.isDelete == true) {
                                if (File(element.uri).exists()) {
                                    File(element.uri).delete()
                                }
                            } else {
                                listImage.add(element)
                            }
                        }
                        image?.image?.clear()
                        image?.image?.addAll(listImage)
                        annotateAdapter.isDelete = false
                        annotateAdapter.isSelect = false
                        annotateAdapter.notifyDataSetChanged()
                        btnDelete.visibility = View.GONE
                        btnNext.visibility = View.GONE
                    }
                }, " ".plus(index1.toString().plus(" files ")))
        }

        icBack.setOnClickListener {
            finish()
        }

        lnThreeDots.setOnClickListener {
            showPopupMore(lnThreeDots)
        }
    }

    private fun showPopupMore(anchor: View?) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.popup_annotate, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }

            contentView.findViewById<TextViewFonts>(R.id.tvSelect)
                .setOnClickListener(View.OnClickListener {
                    annotateAdapter.isSelect = true
                    annotateAdapter.isDelete = false
                    annotateAdapter.notifyDataSetChanged()
                    dismiss()
                    btnDelete.visibility = View.GONE
                    btnNext.visibility = View.GONE
                    for (image in image!!.image!!) {
                        if (image.isCheck == true) {
                            btnNext.visibility = View.VISIBLE
                            return@OnClickListener
                        }
                    }
                })

            contentView.findViewById<TextViewFonts>(R.id.tvDelete)
                .setOnClickListener(View.OnClickListener {
                    annotateAdapter.isDelete = true
                    annotateAdapter.isSelect = false
                    annotateAdapter.notifyDataSetChanged()
                    dismiss()
                    btnDelete.visibility = View.GONE
                    btnNext.visibility = View.GONE
                    for (image in image!!.image!!) {
                        if (image.isDelete == true) {
                            btnDelete.visibility = View.VISIBLE
                            return@OnClickListener
                        }
                    }
                })

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

    override fun onDeleteListener(position: Int, isSelect: Boolean) {
        btnDelete.visibility = View.GONE
        image!!.image!!.get(position).isDelete = isSelect
        for (image in image!!.image!!) {
            if (image.isDelete == true) {
                btnDelete.visibility = View.VISIBLE
                return
            }
        }

        annotateAdapter.notifyDataSetChanged()
    }

    override fun onSelectListener(position: Int, isSelect: Boolean) {
        btnNext.visibility = View.GONE
        image!!.image!!.get(position).isCheck = isSelect
        for (image in image!!.image!!) {
            if (image.isCheck == true) {
                btnNext.visibility = View.VISIBLE
                return
            }
        }
        annotateAdapter.notifyDataSetChanged()
    }
}