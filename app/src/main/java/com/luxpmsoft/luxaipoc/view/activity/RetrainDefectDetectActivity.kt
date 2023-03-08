package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.ImageAdapter
import com.luxpmsoft.luxaipoc.adapter.ViewPagerAdapter
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFramesList
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModels
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_retrain_defect_detect.*
import java.io.*

class RetrainDefectDetectActivity: BaseActivity(), ImageAdapter.OnListener, ViewPagerAdapter.OnListener {
    private var retrainAdapter: ViewPagerAdapter? = null
    private var image: ImageFramesList? = null
    private var imageAdapter: ImageAdapter? = null
    var pos = 0
    private var path = ""
    var trainedModels: TrainedModels? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrain_defect_detect)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            image = Gson().fromJson(bundle.get("image").toString(), ImageFramesList::class.java)
            path = bundle.get("path").toString()
            trainedModels = Gson().fromJson(bundle.get("trained").toString(), TrainedModels::class.java)
            image?.image?.let {
                if (it.isNotEmpty()){
                    it[0].isCheck = true
                }
            }

            Utils.gridLayoutManager(this, rcvImageFrames, 1, GridLayoutManager.HORIZONTAL)
            imageAdapter = ImageAdapter(this, image?.image!!, R.layout.item_image_defect_detect, this, isBackGround = true)
            rcvImageFrames.adapter = imageAdapter

            Utils.gridLayoutManager(this, rcvImageFramesLarge, 1, GridLayoutManager.HORIZONTAL)
            retrainAdapter = ViewPagerAdapter(this, image?.image!!, path, onListener = this)
            viewPager.adapter = retrainAdapter
        }
    }

    fun listener() {
        viewPager.offscreenPageLimit = image?.image?.size!!

        icBack.setOnClickListener {
            finish()
        }

        btnRetrain.setOnClickListener {
            var index = 0
            val file = File(path)
            for (f in file.listFiles()) {
                if (f.name.lowercase().endsWith(".txt")) {
                    try {
                        val inputStream: InputStream = f.inputStream()
                        inputStream?.let {
                            val inputStreamReader = InputStreamReader(inputStream)
                            val bufferedReader = BufferedReader(inputStreamReader)
                            var receiveString: String? = ""
                            val stringBuilder = StringBuilder()
                            while (bufferedReader.readLine().also { receiveString = it } != null) {
                                stringBuilder.append(receiveString)
                            }
                            inputStream.close()
                            stringBuilder?.let {
                                if (it.isNotEmpty()) {
                                    val strs = stringBuilder.toString().split(";")
                                    if (strs.isNotEmpty()) {
                                        index += 1
                                    }
                                }
                            }
                        }
                    } catch (e: FileNotFoundException) {
                        Log.e("login activity", "File not found: $e")
                    } catch (e: IOException) {
                        Log.e("login activity", "Can not read file: $e")
                    }
                }
            }
            if (index == image?.image?.size) {
                val intent = Intent(this, LoaderScreenActivity::class.java)
                intent.putExtra("trained", Gson().toJson(trainedModels))
                intent.putExtra("path", path)
                startActivity(intent)
            } else {
                Toast.makeText(this, resources.getString(R.string.str_annotate_all_images), Toast.LENGTH_SHORT).show()
            }
        }

        scrollLeft.setOnClickListener {
            if (pos > 0) {
                image?.image?.get(pos)?.isCheck = false
                pos -= 1
                rcvImageFrames.scrollToPosition(pos)

                if (pos == 0) {
                    scrollLeft.visibility = View.GONE
                }
                if (scrollRight.visibility == View.GONE) {
                    scrollRight.visibility = View.VISIBLE
                }
                image?.image?.get(pos)?.isCheck = true
                viewPager.currentItem = pos
                imageAdapter?.notifyDataSetChanged()
            }
        }

        scrollRight.setOnClickListener {
            if (pos < image?.image?.size!!-1) {
                if (scrollLeft.visibility == View.GONE) {
                    scrollLeft.visibility = View.VISIBLE
                }
                image?.image?.get(pos)?.isCheck = false
                pos += 1

                rcvImageFrames.scrollToPosition(pos)
                if (pos == image?.image?.size!!-1) {
                    scrollRight.visibility = View.GONE
                }

                image?.image?.get(pos)?.isCheck = true
                viewPager.currentItem = pos
                imageAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onDeleteListener(posi: Int ,position: ImageFrameModel) {
        runOnUiThread {
            try {
                image?.image?.get(pos)?.isCheck = false
                pos = posi
                image?.image?.get(pos)?.isCheck = true
                viewPager.currentItem = pos
                imageAdapter?.notifyDataSetChanged()
                if (pos <= 0) {
                    scrollLeft.visibility = View.GONE
                } else {
                    scrollLeft.visibility = View.VISIBLE
                }

                if (pos >= image?.image?.size!! -1) {
                    scrollRight.visibility = View.GONE
                } else {
                    scrollRight.visibility = View.VISIBLE
                }
            } catch (e: IndexOutOfBoundsException) {
                e.message
            }
        }
    }

    override fun onItemListener(position: Int) {
        runOnUiThread {
            try {
                image?.image?.get(pos)?.isCheck = false
                pos = position
                image?.image?.get(pos)?.isCheck = true
                viewPager.currentItem = pos
                imageAdapter?.notifyDataSetChanged()
                if (pos <= 0) {
                    scrollLeft.visibility = View.GONE
                } else {
                    scrollLeft.visibility = View.VISIBLE
                }

                if (pos >= image?.image?.size!! -1) {
                    scrollRight.visibility = View.GONE
                } else {
                    scrollRight.visibility = View.VISIBLE
                }
            } catch (e: IndexOutOfBoundsException) {
                e.message
            }
        }
    }

    override fun onDrawListener(position: Int, isDraw: Boolean?) {
        image?.image?.get(position)?.isAnnotate = isDraw
        imageAdapter?.notifyItemChanged(position)
    }
}