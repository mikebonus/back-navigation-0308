package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.ar.sceneform.collision.Plane
import com.google.ar.sceneform.collision.Ray
import com.google.ar.sceneform.collision.RayHit
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.MeasurementAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.ConstantAPI
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.arcore.APIARUtils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import com.luxpmsoft.luxaipoc.model.reconstruction.RenameModelRequest
import com.luxpmsoft.luxaipoc.model.reconstruction.RenameModelResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ITap
import io.github.sceneview.node.LoadingNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.renderable.Renderable
import kotlinx.android.synthetic.main.activity_show_object_organization.*
import java.io.File
import java.text.DecimalFormat
import kotlin.math.sqrt

class ShowObjectOrganizationActivity : AppCompatActivity(), ITap{
    var name = ""
    var ar = ""
    var data: Rows? = null
    var p1: Vector3? = null
    var p2: Vector3? = null
    var modelNode: ModelNode? = null
    var listMeasurement: ArrayList<String> = ArrayList()
    companion object {
        const val TAG = "ShowObjectOrganizationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_object_organization)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
    }

    private fun listener() {
        icBack.setOnClickListener {
            finish()
        }

        icMore.setOnClickListener {
            DialogFactory.dialogView(
                this,
                object : DialogFactory.Companion.DialogListener.ViewModelListener {
                    override fun share() {
                        if (checkExistFile()) {
                            createShare()
                        } else {
                            downloadModel(BuildConfig.URL_OA3D+ "reconstruction/model/"+data?.reconstructionID)
                        }
                    }

                    override fun copyLink() {
                        val clipboard: ClipboardManager =
                            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("label", BuildConfig.URL_OA3D+ "reconstruction/model/"+data?.reconstructionID)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(
                            this@ShowObjectOrganizationActivity,
                            "Link copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun download() {
                        if (ar.isNotEmpty()) {
                            getModelUrl(name)
                        } else {
                            if (!checkExistFile()) {
                                downloadModel(BuildConfig.URL_OA3D+ "reconstruction/model/"+data?.reconstructionID)
                            }
                        }
                    }

                    override fun deleteModel() {
                        DialogFactory.dialogDelete(
                            this@ShowObjectOrganizationActivity,
                            object : DialogFactory.Companion.DialogListener.Delete {
                                override fun delete() {
                                    data?.reconstructionID?.let { it1 -> deleteModel(it1) }
                                }
                            }, " model ".plus(data?.modelName))
                    }

                    override fun renameModel() {
                        DialogFactory.dialogRenameModel(
                            this@ShowObjectOrganizationActivity,
                            object : DialogFactory.Companion.DialogListener.RenameModel {
                                override fun renameModel(newName: String) {
                                    rename(newName)
                                }
                            }, resources.getString(R.string.rename_model))
                    }
                })
        }

        tvViewInYourSpace.setOnClickListener {
            data?.reconstructionID?.let { it1 -> startViewSpace(it1) }
        }

        btResetMeasurement.setOnClickListener {
//            for (node in nodeList!!) {
//                sceneView.scene.removeChild(node)
//                dragTransformableNode?.removeChild(node)
//            }
//            nodeList?.clear()
//            btResetMeasurement.visibility = View.GONE
//            isResetMeasurement = false
        }

        val bundle = intent.extras
        if (bundle != null) {
            data = Gson().fromJson(bundle.getString("data"), Rows::class.java)
            tvNameModel.text = data?.modelName
            tvType.text = data?.uploadDateTime?.let { MyUtils.convertToLocalTime(it)}

            if (bundle.getString("organization") != null) {
                tvNameModel.visibility = View.GONE
                tvType.visibility = View.GONE
                tvTime.text = data?.uploadDateTime?.let { MyUtils.convertDateTimeHH(it)}
                data?.fileSize?.let {
                    tvSize.text = "Size: ".plus(DecimalFormat("##.##").format(it.toDouble()/(1024*1024))).plus(" MB")
                }
            }

            if (data!!.scanningType!!.scanningTypeName.equals("Body Pose")) {
                lineBodyMeasure.visibility = View.VISIBLE
                tvHeadTitle.visibility = View.VISIBLE
                tvHeadMeasure.visibility = View.VISIBLE
                tvShoulderTitle.visibility = View.VISIBLE
                tvShoulderMeasure.visibility = View.VISIBLE
                tvWaistTitle.visibility = View.VISIBLE
                tvWaistMeasure.visibility = View.VISIBLE
                tvHipsTitle.visibility = View.VISIBLE
                tvHipsMeasure.visibility = View.VISIBLE
                tvArmSpanTitle.visibility = View.VISIBLE
                tvArmSpanMeasure.visibility = View.VISIBLE
                tvChestTitle.visibility = View.VISIBLE
                tvChestMeasure.visibility = View.VISIBLE
                tvTorsoTitle.visibility = View.VISIBLE
                tvTorsoMeasure.visibility = View.VISIBLE
                tvLegTitle.visibility = View.VISIBLE
                tvLegMeasure.visibility = View.VISIBLE

                data?.bodyPose?.let {
                    tvHeadMeasure.text = DecimalFormat("###").format(it.poseMetaData?.head_vlen?.times(100)).plus(" cm")
                    tvShoulderMeasure.text = DecimalFormat("###").format(it.poseMetaData?.shoulder_hlen?.times(100)).plus(" cm")
                    tvWaistMeasure.text = DecimalFormat("###").format(it.poseMetaData?.waist?.times(100)).plus(" cm")
                    tvHipsMeasure.text = DecimalFormat("###").format(it.poseMetaData?.hips?.times(100)).plus(" cm")
                    tvArmSpanMeasure.text = DecimalFormat("###").format(it.poseMetaData?.arm_span_hlen?.times(100)).plus(" cm")
                    tvChestMeasure.text = DecimalFormat("###").format(it.poseMetaData?.chest?.times(100)).plus(" cm")
                    tvTorsoMeasure.text = DecimalFormat("###").format(it.poseMetaData?.torso_vlen?.times(100)).plus(" cm")
                    tvLegMeasure.text = DecimalFormat("###").format(it.poseMetaData?.leg_vlen?.times(100)).plus(" cm")

                    tvWeight.text = DecimalFormat("###").format(it.poseMetaData?.mass).plus(" Ibs")
                    tvWeight1.text = DecimalFormat("###").format(it.poseMetaData?.mass).plus(" Ibs")
                    tvHeight.text = DecimalFormat("###").format(it.poseMetaData?.height_vlen?.times(100)).plus(" cm")
                }
            } else {
                lineOrganization.visibility = View.VISIBLE
            }

            if (checkExistFile()) {
                loadModelUrl(
                    this,
                    BuildConfig.URL_OA3D + "reconstruction/model/" + data?.reconstructionID,
                    sceneView,
                    data?.reconstructionID.toString(),
                    data?.modelName.toString())
            } else {
                MyUtils.showProgress(this, flProgressRename)
                downloadModel(BuildConfig.URL_OA3D+ "reconstruction/model/"+data?.reconstructionID)
            }
        }
    }

//    fun configCameraScene() {
//        sceneView.scene.camera.localPosition = Vector3(0F, 0F, 3F)
//        sceneView.scene.camera.worldPosition = Vector3(0F, 0F, 3F)
//    }

    fun startViewSpace(id: String) {
        val intent = Intent(this, ViewSpaceActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    fun createShare() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val screenshotUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+data?.reconstructionID+".gltf")
        shareIntent.type = "model/gltf+json"
        shareIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(shareIntent, "Share it"))
    }

    fun checkExistFile() : Boolean {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+data?.reconstructionID+".gltf")
        if (file.exists()) {
            return true
        }
        return false
    }

    fun rename(newName: String) {
        MyUtils.showProgress(this, flProgressRename)
        val rename = RenameModelRequest()
        rename.reconstructionID = data?.reconstructionID
        rename.newModelName = newName
        APIOpenAirUtils.renameModel((application as LidarApp).prefManager!!.getToken(), rename, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@ShowObjectOrganizationActivity, flProgressRename)
                val r = result as RenameModelResponse
                tvNameModel.text = r.body?.modelName
                tvType.text = r.body?.reconstructionType
                Toast.makeText(
                    this@ShowObjectOrganizationActivity,
                    getString(R.string.rename_success),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@ShowObjectOrganizationActivity, flProgressRename)
                MyUtils.toastError(this@ShowObjectOrganizationActivity, error as ErrorModel)
            }
        })
    }

    fun deleteModel(reconstructionID: String) {
        MyUtils.showProgress(this, flProgressRename)
        APIOpenAirUtils.deleteModel((application as LidarApp).prefManager!!.getToken(), reconstructionID, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@ShowObjectOrganizationActivity, flProgressRename)
                Toast.makeText(
                    this@ShowObjectOrganizationActivity,
                    "Delete Success",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@ShowObjectOrganizationActivity, flProgressRename)
                MyUtils.toastError(this@ShowObjectOrganizationActivity, error as ErrorModel)
            }
        })
    }

    fun getModelUrl(sessionId: String?) {
        APIARUtils.getModelUrl(ConstantAPI.USER_ID, sessionId!!, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as String
                try {
                    downloadModel(data)
                } catch (e: Exception) {
                    e.message
                }
                Toast.makeText(
                    this@ShowObjectOrganizationActivity,
                    "Download Success",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(error: Any?) {
                Toast.makeText(
                    this@ShowObjectOrganizationActivity,
                    "Get Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        try {
//            sceneView.resume()
            sceneView.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_white))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sceneView?.let {
//            it.pause()
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
        val download:DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
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
                        MyUtils.hideProgress(this, flProgressRename)
                        val handler = Handler()
                        handler.postDelayed(Runnable {
                            // Actions to do after 1 seconds
                            loadModelUrl(
                                this,
                                BuildConfig.URL_OA3D + "reconstruction/model/" + data?.reconstructionID,
                                sceneView,
                                data?.reconstructionID.toString(),
                                data?.modelName.toString())
                        }, 1000)

                    }
                }
                cursor.close()
            }
        }.start()
    }

    fun loadModelUrl(activity: Activity, url: String?, sceneView: SceneView, reconstructionId: String, name: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+reconstructionId+".gltf")
        Log.d(TAG, "loadModelUrl: " + file.absolutePath)
//        ModelRenderable.builder()
//            .setSource(
//                activity,
//                Uri.parse(file.toString())
//            )
//            .setIsFilamentGltf(true)
//            .build()
//            .thenAccept { modelRenderable ->
//
////                addNodeToScene(modelRenderable)
//            }
//            .exceptionally { throwable ->
//                val toast = Toast.makeText(
//                    activity,
//                    "Unable to load model ".plus(name),
//                    Toast.LENGTH_LONG
//                )
//                toast.setGravity(Gravity.CENTER, 0, 0)
//                toast.show()
//                null
//            }
//        MyUtils.showProgress(this, flProgressRename)
        val modelNode = ModelNode(
            position = Position(z = 0.0f),
            rotation = Rotation(x = 0.0f),
        )
//        modelNode.parent = sceneView
        sceneView.addChild(modelNode)
//        sceneView.cameraNode.transform = lookAt(
//            eye = modelNode.worldPosition.let {
//                Position(x = it.x, y = it.y + 0.5f, z = it.z + 2.0f)
//            },
//            target = modelNode.worldPosition,
//            up = Direction(y = 1.0f)
//        )
        lifecycleScope.launchWhenCreated {
            modelNode.loadModel(
                context = activity,
                lifecycle = lifecycle,
                glbFileLocation = file.path,
                scaleToUnits = 1.1f,
                autoAnimate = false,
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0f)
            )

//            delay(1500)

//            sceneView.cameraNode.smooth(
//                lookAt(
//                    eye = modelNode.worldPosition.let {
//                        Position(x = it.x - 0.4f, y = it.y + 0.4f, z = it.z - 0.6f)
//                    },
//                    target = modelNode.worldPosition,
//                    up = Direction(y = 1.0f)
//                ),
//                speed = 0.7f
//            )
        }
    }

    private fun getDistance(): Double {
        val dx = 0.44697297 - (-0.13345993)
        val dy = (-0.6791497) - (-0.35627598)
        val dz = (-0.5024998) - (-0.50249994)
        return sqrt((dx * dx + dz * dz + dy * dy).toDouble())
    }

    override fun onTap(motionEvent: MotionEvent, renderable: Renderable?) {
//        sceneView.pickNode(motionEvent.x.toInt(), motionEvent.y.toInt()) { node, renderable ->
//            val ray: Ray = sceneView.cameraNode.motionEventToRay(motionEvent)
//            modelNode?.let {
//                val point = dispatchTouchEventToView(it, motionEvent)
//                val view = LoadingNode(position = Position(x = point.x, y = point.y, z = point.z),
//                    lifecycle = lifecycle, layoutResId = R.layout.item_dots, context = this)
//                modelNode?.addChild(view)
//                if (p1 == null) {
//                    p1 = point
//                } else {
//                    p2= point
//
//                    addLineBetweenHits(motionEvent, p1, p2)
//                    p1 = null
//                    p2 = null
//                }
//            }
//        }
    }

    private fun addLineBetweenHits(motionEvent: MotionEvent, point1: Vector3?, point2: Vector3?) {
        try {
            val `val` = motionEvent.actionMasked
            val axisVal = motionEvent.getAxisValue(
                MotionEvent.AXIS_X,
                motionEvent.getPointerId(motionEvent.pointerCount - 1)
            )
            Log.e("Values:", `val`.toString() + axisVal.toString())
            val difference = Vector3.subtract(point1, point2)
            val directionFromTopToBottom = difference.normalized()
            val rotationFromAToB =
                Quaternion.lookRotation(directionFromTopToBottom, Vector3.up())
            val location = Vector3.add(point1, point2).scaled(.5f)
//            val view = LoadingNode(position = Position(x = location.x, y = location.y, z = location.z),
//                lifecycle = lifecycle, layoutResId = R.layout.text_line, context = this)
//            view.with = difference.length().toInt()
//            modelNode?.addChild(view)

            val viewDistance = LoadingNode(position = Position(x = location.x, y = location.y, z = location.z),
                lifecycle = lifecycle, layoutResId = R.layout.text_distance, context = this)
            viewDistance.text = MyUtils.getDistance(p1!!, p2!!).plus("m")
            modelNode?.addChild(viewDistance)
        } catch (e: IllegalArgumentException) {
            e.message
        } catch (e: Exception) {
            e.message
        }
    }


    fun dispatchTouchEventToView(node: Node, motionEvent: MotionEvent): Vector3 {
        var point = Vector3()
        val scene = node.getSceneViewInternal()
        val pointerCount = motionEvent.pointerCount
        val pointerProperties = arrayOfNulls<PointerProperties>(pointerCount)
        val pointerCoords = arrayOfNulls<PointerCoords>(pointerCount)
        val nodeTransformMatrix = node.transformationMatrix
        val nodePosition = Vector3()
        nodeTransformMatrix.decomposeTranslation(nodePosition)
        val nodeScale = Vector3()
        nodeTransformMatrix.decomposeScale(nodeScale)
        val nodeRotation = Quaternion()
        nodeTransformMatrix.decomposeRotation(nodeScale, nodeRotation)
        val nodeForward = Quaternion.rotateVector(nodeRotation, Vector3.forward())
        val nodeBack = Quaternion.rotateVector(nodeRotation, Vector3.back())
        val plane = Plane(nodePosition, nodeForward)
        val rayHit = RayHit()

        // Also cast a ray against a back-facing plane because we render the view as double-sided.
        val backPlane = Plane(nodePosition, nodeBack)

        // Convert the pointer coordinates for each pointer into the view's local coordinate space.
        for (i in 0 until pointerCount) {
            val props = PointerProperties()
            val coords = PointerCoords()
            motionEvent.getPointerProperties(i, props)
            motionEvent.getPointerCoords(i, coords)
            val camera = scene?.cameraNode
            val ray = camera?.screenPointToRay(coords.x, coords.y)
            if (plane.rayIntersection(ray, rayHit)) {
                point = rayHit.point
            } else {
                coords.clear()
                props.clear()
            }
            pointerProperties[i] = props
            pointerCoords[i] = coords
        }

        // We must copy the touch event with the new coordinates and dispatch it to the view.
        val me = MotionEvent.obtain(
            motionEvent.downTime,
            motionEvent.eventTime,
            motionEvent.action,
            pointerCount,
            pointerProperties,
            pointerCoords,
            motionEvent.metaState,
            motionEvent.buttonState,
            motionEvent.xPrecision,
            motionEvent.yPrecision,
            motionEvent.deviceId,
            motionEvent.edgeFlags,
            motionEvent.source,
            motionEvent.flags
        )
        return point
    }
}