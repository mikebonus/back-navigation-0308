package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.ImageAdapter
import com.luxpmsoft.luxaipoc.adapter.ViewPagerAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.defectdetect.APIDefectDetectUtils
import com.luxpmsoft.luxaipoc.model.defect_detect.DefectDetectResponse
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFramesList
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModels
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.ProgressRequestBody
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import com.luxpmsoft.luxaipoc.widget.decoder.Frame
import com.luxpmsoft.luxaipoc.widget.decoder.FrameExtractor
import com.luxpmsoft.luxaipoc.widget.decoder.IVideoFrameExtractor
import com.luxpmsoft.luxaipoc.widget.decoder.URIPathHelper
import kotlinx.android.synthetic.main.activity_frames_preview.*
import kotlinx.android.synthetic.main.activity_frames_preview.lnProgress
import kotlinx.android.synthetic.main.activity_frames_preview.progressBar
import kotlinx.android.synthetic.main.activity_loader_screen.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FramesPreviewActivity: BaseActivity(), IVideoFrameExtractor, ImageAdapter.OnListener, ProgressRequestBody.UploadCallbacks,
    ViewPagerAdapter.OnListener{
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    var nameFolderFile = ""
    private var imagePaths = ArrayList<ImageFrameModel>()
    private lateinit var imageAdapter: ImageAdapter
    var totalSavingTimeMS: Long = 0
    var pos = 0
    var index = 0
    var trainedModels: TrainedModels? = null
    var mFolderName = "detect_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    var mDirectory: File? = null
    private var framesAdapter: ViewPagerAdapter? = null
    var isProgress = false
    var indexProcess = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        setContentView(R.layout.activity_frames_preview)
        init()
        listener()
    }

    fun init() {
        mDirectory = MyUtils.getOutputDefectDirectory(this, mFolderName)
        val bundle = intent.extras
        if (bundle != null) {
            nameFolderFile = bundle.getString("pathFile").toString()
            trainedModels = Gson().fromJson(bundle.get("trained").toString(), TrainedModels::class.java)
        }

        MyUtils.showProgress(this, flProgress)
        val uriPathHelper = URIPathHelper()
        val videoInputPath = uriPathHelper.getPath(this, Uri.parse(nameFolderFile)).toString()
        val videoInputFile = File(videoInputPath)

        val frameExtractor = FrameExtractor(this)
        executorService.execute {
            try {
                frameExtractor.extractFrames(videoInputFile.absolutePath)
            } catch (exception: Exception) {
                exception.printStackTrace()
                this.runOnUiThread {
                    Toast.makeText(this, "Failed to extract frames", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun listener() {
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

        btnRunTest.setOnClickListener {
            if (imagePaths.size > 0) {
                requestMulti(imagePaths)
            }
        }
    }

    fun scrollRight() {
        if (pos < imagePaths.size-1) {
            if (scrollLeft.visibility == View.GONE) {
                scrollLeft.visibility = View.VISIBLE
            }
            imagePaths[pos].isCheck = false
            pos += 1

            rcvImageFrames.scrollToPosition(pos)
            if (pos == imagePaths.size-1) {
                scrollRight.visibility = View.GONE
            }

            imagePaths[pos].isCheck = true
            viewPager.currentItem = pos
            imageAdapter.notifyDataSetChanged()
        }
    }

    fun scrollLeft() {
        if (pos > 0) {
            imagePaths[pos].isCheck = false
            pos -= 1
            rcvImageFrames.scrollToPosition(pos)

            if (pos == 0) {
                scrollLeft.visibility = View.GONE
            }
            if (scrollRight.visibility == View.GONE) {
                scrollRight.visibility = View.VISIBLE
            }
            imagePaths[pos].isCheck = true
            viewPager.currentItem = pos
            imageAdapter.notifyDataSetChanged()
        }
    }

    override fun onCurrentFrameExtracted(currentFrame: Frame, maxFrame: Int, decodeCount: Int) {
        val startSavingTime = System.currentTimeMillis()
        if (currentFrame.position == 1 || currentFrame.position == index || currentFrame.position == maxFrame){
            // 1. Convert frame byte buffer to bitmap
            val imageBitmap = MyUtils.fromBufferToBitmap(currentFrame.byteBuffer, currentFrame.width, currentFrame.height)

//        // 2. Get the frame file in app external file directory
//        val allFrameFileFolder = File(this.getExternalFilesDir(null), UUID.randomUUID().toString())
//        if (!allFrameFileFolder.isDirectory) {
//            allFrameFileFolder.mkdirs()
//        }
            val frameFile = File(mDirectory, "frame_num_${currentFrame.timestamp.toString().padStart(10, '0')}.jpeg")

            // 3. Save current frame to storage
            imageBitmap?.let {
                val savedFile = MyUtils.saveImageToFile(it, frameFile)
                savedFile?.let {
                }
            }

            totalSavingTimeMS += System.currentTimeMillis() - startSavingTime
            index += 14
            this.runOnUiThread {
                Log.e("OKOK", "Extract ${currentFrame.position} frames")
//            infoTextView.text = "Extract ${currentFrame.position} frames"
            }
        }
    }

    override fun onAllFrameExtracted(processedFrameCount: Int, processedTimeMs: Long) {
        this.runOnUiThread {
            MyUtils.hideProgress(this@FramesPreviewActivity, flProgress)

            for(item in mDirectory?.listFiles()!!.sorted()) {
                imagePaths.add(ImageFrameModel(item.path, false))
            }

            imagePaths.get(0).isCheck = true
            Utils.gridLayoutManager(this, rcvImageFrames, 1, GridLayoutManager.HORIZONTAL)
            imageAdapter = ImageAdapter(this, imagePaths, R.layout.item_image_frame, this, true)
            rcvImageFrames.adapter = imageAdapter
            framesAdapter = ViewPagerAdapter(this,imagePaths, mDirectory!!.path, true, onListener = this)
            viewPager.adapter = framesAdapter
        }
    }

    override fun onDeleteListener(posi: Int ,position: ImageFrameModel) {
        DialogFactory.dialogConfirmDeleteFrame(
            this,
            object : DialogFactory.Companion.DialogListener.Rerecord {
                override fun reRecord() {
                    for(item in mDirectory?.listFiles()!!) {
                        if (position.uri.toString().lowercase().contains(item.name.lowercase())) {
                            val file = File(position.uri.toString())
                            if (file.exists()) {
                                file.delete()
                            }
                        }
                    }

                    imagePaths.remove(position)
                    if (pos >= 0 && pos <= imagePaths.size - 1) {
                    } else {
                        pos = imagePaths.size - 1
                    }

                    if (imagePaths.size == 1) {
                        pos = 0
                        scrollRight.visibility = View.GONE
                        scrollLeft.visibility = View.GONE
                    }

                    if (imagePaths.isNotEmpty()) {
                        imagePaths[pos].isCheck = true
                    }

                    imageAdapter.notifyDataSetChanged()
                    framesAdapter?.notifyDataSetChanged()
                }
            })
    }

    override fun onItemListener(position: Int) {
        runOnUiThread {
            try {
                if (pos <= imagePaths.size - 1) {
                    imagePaths.get(pos)?.isCheck = false
                }
                pos = position
                imagePaths.get(pos)?.isCheck = true
                viewPager.currentItem = pos
                imageAdapter?.notifyDataSetChanged()
                if (pos <= 0) {
                    scrollLeft.visibility = View.GONE
                } else {
                    scrollLeft.visibility = View.VISIBLE
                }

                if (pos >= imagePaths.size!! -1) {
                    scrollRight.visibility = View.GONE
                } else {
                    scrollRight.visibility = View.VISIBLE
                }
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }
        }
    }

    //upload photo
    fun requestMulti(listImage: ArrayList<ImageFrameModel>?) {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
            this,
            OnSuccessListener { instanceIdResult: InstanceIdResult ->
                val newToken = instanceIdResult.token
                progressBar.progress = 0
                progressBar.max = listImage!!.size
                progressBar.visibility = View.VISIBLE
                lnUpload.visibility = View.GONE
                MyUtils.showProgress(this, lnProgress)
                val builder: MultipartBody.Builder =
                    MultipartBody.Builder().setType(MultipartBody.FORM)
                for(item in listImage!!) {
                    val path = File(item.uri.toString())
                    val file = ProgressRequestBody(path, "application/image".toMediaType(), this)
                    builder.addFormDataPart(
                        "files", path.name,
                        file
                    )
                }

                trainedModels?.uid?.let {
                    builder.addFormDataPart(
                        "user_id", it
                    )
                }

                trainedModels?.sessionId?.let {
                    builder.addFormDataPart(
                        "session_id", it
                    )
                }

                builder.addFormDataPart(
                    "return_filter", "defectonly"
                )

                builder.addFormDataPart(
                    "firebase_device_token", newToken
                )

                val requestBody: RequestBody = builder.build()
                APIDefectDetectUtils.testMultiDetection((application as LidarApp).prefManager!!.getToken(), requestBody, object : APIInterface.onDelegate {
                    override fun onSuccess(result: Any?) {
                        val data = result as DefectDetectResponse
                        progressBar.progress = listImage.size
                        DialogFactory.dialogRetrainSuccess(
                            this@FramesPreviewActivity,
                            object : DialogFactory.Companion.DialogListener.Retrain {
                                override fun reTrain() {
                                    removeFile()
                                    startHome()
                                }
                            }, resources.getString(R.string.str_run_test))
                    }

                    override fun onError(error: Any?) {
                        lnUpload.visibility = View.VISIBLE
                        MyUtils.hideProgress(this@FramesPreviewActivity, lnProgress)
                        Toast.makeText(
                            this@FramesPreviewActivity,
                            "File Uploaded Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            })
    }

    fun removeFile() {
        mDirectory?.let {
            it.listFiles()?.let {
                for (f in it) {
                    f.delete()
                }
            }
            it.delete()
        }
    }

    fun downLoadZip(filePath: String, count: Int) {
        APIDefectDetectUtils.downloadZip(filePath, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as ResponseBody
                DialogFactory.dialogTestDetection(
                    this@FramesPreviewActivity,
                    object : DialogFactory.Companion.DialogListener.TestDetection {
                        override fun show() {
//                            MyUtils.hideProgress(this@FramesPreviewActivity, flProgress)
                                val file = File(mDirectory, mFolderName+".zip")
                                val fileOutputStream = FileOutputStream(file)
                                fileOutputStream.write(data.bytes())
                                fileOutputStream.close()
                                val imageList = ImageFramesList(imagePaths)
                                val intent = Intent(this@FramesPreviewActivity, DefectDetectActivity::class.java)
                                intent.putExtra("image", Gson().toJson(imageList))
                                intent.putExtra("folderZip", file.path)
                                intent.putExtra("path", mDirectory?.path)
                                intent.putExtra("trained", Gson().toJson(trainedModels))
                                startActivity(intent)
                        }

                        override fun goHome() {
                            MyUtils.hideProgress(this@FramesPreviewActivity, flProgress)
                            startHome()
                        }

                    }, count)
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@FramesPreviewActivity, flProgress)
                Toast.makeText(
                    this@FramesPreviewActivity,
                    "File Uploaded Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun startHome() {
        val intent = Intent(this@FramesPreviewActivity, HomeOrganizationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    override fun onProgressUpdate(percentage: Int) {
        if (isProgress && percentage == 0) {
            isProgress = false
        }

        if (!isProgress && percentage == 99) {
            isProgress = true
            indexProcess++
            progressBar.progress = indexProcess
        }
    }

    override fun onError() {

    }

    override fun onFinish() {

    }

    override fun onDrawListener(position: Int, isDraw: Boolean?) {

    }
}