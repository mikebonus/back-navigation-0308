package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.webkit.URLUtil
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.google.android.filament.gltfio.Animator
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ModelRenderable
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.utils.MyUtils
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import kotlinx.android.synthetic.main.activity_view_space.*
import java.io.File
import java.util.*

class ViewSpaceActivity: AppCompatActivity() {
    private var arFragment: ArSceneView? = null
    private var objectRenderable: ModelRenderable? = null
//    var anchorNode: AnchorNode? = null
    var reconstructionID: String? = ""
    var modelNode: ArModelNode? = null
    var modelIndex = 0

    private class AnimationInstance internal constructor(
        var animator: Animator,
        index: Int,
        var startTime: Long
    ) {
        var duration: Float
        var index: Int

        init {
            duration = animator.getAnimationDuration(index)
            this.index = index
        }
    }
    private val animators: ArrayList<AnimationInstance> = ArrayList()

    private val colors = Arrays.asList(
        Color(0F, 0F, 0F, 1F),
        Color(1F, 0F, 0F, 1F),
        Color(0F, 1F, 0F, 1F),
        Color(0F, 0F, 1F, 1F),
        Color(1F, 1F, 0F, 1F),
        Color(0F, 1F, 1F, 1F),
        Color(1F, 0F, 1F, 1F),
        Color(1F, 1F, 1F, 1F)
    )
    private var nextColor = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_space)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
    }

    fun init() {
//        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment?
        val bundle = intent.extras
        if (bundle != null) {
            reconstructionID = bundle.getString("id")
            newModelNode()
//            checkExistFile()
            // Set tap listener
//            arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
//                val anchor = hitResult.createAnchor()
////                if (anchorNode == null) {
////                    anchorNode = AnchorNode(anchor)
////                    anchorNode?.setParent(arFragment!!.arSceneView.scene)
////                    createModel()
////                }
//            }
        }
    }

    fun newModelNode() {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+reconstructionID+".gltf")

        modelNode?.takeIf { !it.isAnchored }?.let {
            sceneView.removeChild(it)
            it.destroy()
        }

        modelNode = ArModelNode().apply {
            loadModelAsync(
                context = this@ViewSpaceActivity,
                lifecycle = lifecycle,
                glbFileLocation = file.path,
                autoAnimate = false,
                scaleToUnits = 1f,
                // Place the model origin at the bottom center
                centerOrigin = Position(y = -1.0f),

            ) {
            }
//            onPoseChanged = { node, _ ->
//                placeModelButton.isGone = node.isAnchored || !node.isTracking
//            }
        }
        sceneView.addChild(modelNode!!)
        // Select the model node by default (the model node is also selected on tap)
        sceneView.selectedNode = modelNode
    }

    private fun createModel() {
        try {
//            if (anchorNode != null) {
//                val node = TransformableNode(arFragment!!.transformationSystem)
//                node.scaleController.maxScale = 1f
//                node.scaleController.minScale = 0.75f
//                node.setParent(anchorNode)
//                node.renderable = objectRenderable
//                node.select()
//            }
        } catch (e: Exception) {
            Toast.makeText(this, "Render wrong", Toast.LENGTH_SHORT).show()

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun loadModel() {
        try {
//            val screenshotUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+reconstructionID+".gltf")
//            MyUtils.showProgress(this, flProgress)
//            ModelRenderable.builder()
//                .setSource(this,screenshotUri)
//                .setIsFilamentGltf(true)
//                .build()
//                .thenAccept { renderable: ModelRenderable ->
//                    MyUtils.hideProgress(this, flProgress)
//                    objectRenderable = renderable
//                    Toast.makeText(this, "Model open", Toast.LENGTH_SHORT).show()
//                }
//                .exceptionally { throwable: Throwable? ->
//                    MyUtils.hideProgress(this, flProgress)
//                    finish()
//                    Toast.makeText(this, "Model can't load", Toast.LENGTH_SHORT).show()
//                    null
//                }
        } catch (e: Exception) {
            e.printStackTrace()
            MyUtils.hideProgress(this, flProgress)
            finish()
            Toast.makeText(this, "Model can't load", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("Range")
    fun downloadModel(dat: String) {
        val downloadManager: DownloadManager.Request = DownloadManager.Request(Uri.parse(dat))
        val title = URLUtil.guessFileName(dat, null, "model/gltf+json").replace(".bin", ".gltf")
        downloadManager.setTitle(title)
        downloadManager.setDescription("Downloading file please wait...")
        downloadManager.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
        val download: DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        var downloadId = download.enqueue(downloadManager)
        Thread {
            var downloading = true
            while (downloading) {
                val q = DownloadManager.Query()
                q.setFilterById(downloadId)
                val cursor: Cursor = download.query(q)
                cursor.moveToFirst()
                val bytes_downloaded = cursor.getInt(
                    cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                )
                val bytes_total =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val dl_progress = (bytes_downloaded * 100L / bytes_total).toInt()
                runOnUiThread {
                    if (dl_progress == 100) {
                        MyUtils.hideProgress(this, flProgress)
                        loadModel()
                    }
                }
                cursor.close()
            }
        }.start()
    }

    fun checkExistFile() {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+reconstructionID+".gltf")
        if (file.exists()) {
            loadModel()
        } else {
            MyUtils.showProgress(this, flProgress)
            downloadModel(BuildConfig.URL_OA3D+ "reconstruction/model/"+reconstructionID)
        }
    }
}