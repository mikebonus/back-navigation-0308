package com.luxpmsoft.luxaipoc.view.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.recentmodel.CadFileRows
import com.luxpmsoft.luxaipoc.utils.MyUtils
import dev.romainguy.kotlin.math.lookAt
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlinx.android.synthetic.main.dialog_show_full_model.*
import kotlinx.coroutines.delay
import java.io.File

class ShowFullModelActivity: AppCompatActivity() {
    var cadFile: CadFileRows? = null
    var htmlFile: String? = ""
    var isLoadHtml = false
    var p1: Vector3? = null
    var p2: Vector3? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Disable dark mode
        setContentView(R.layout.dialog_show_full_model)
        MyUtils.setStatusBarTransparentFlagBlack(this)

        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        bundle?.let {
            cadFile = Gson().fromJson(it.get("cadFile").toString(), CadFileRows::class.java)
            htmlFile = it.getString("htmlFile")
            isLoadHtml = it.getBoolean("isLoadHtml")
        }
    }

    fun listener() {
        icClose.setOnClickListener {
            finish()
        }

        lineResetMeasurement.setOnClickListener {
//            if (nodeList!!.isNotEmpty()) {
//                for (node in nodeList!!) {
//                    sceneView.scene.removeChild(node)
//                    dragTransformableNode?.removeChild(node)
//                }
//                nodeList?.clear()
//            }
        }

        if (isLoadHtml) {
            MyUtils.loadModel(this, webView, "", htmlFile)
            sceneView.visibility = View.GONE
            webView.visibility = View.VISIBLE
        } else {
            sceneView.visibility = View.VISIBLE
            webView.visibility = View.GONE
            cadFile?.reconstructions?.let {
                for (model in it) {
                   loadModelUrl(
                        this,
                        BuildConfig.URL_OA3D + "reconstruction/model/" + model.reconstructionID,
                        sceneView,
                        model.reconstructionID.toString(),
                        model.modelName.toString()
                    )
                }
            }
//            sceneView.resume()
        }
    }

    override fun onResume() {
        super.onResume()
        sceneView.setBackgroundDrawable(resources.getDrawable(R.drawable.sceneview_info_background))

    }

    fun loadModelUrl(activity: Activity, url: String?, sceneView: io.github.sceneview.SceneView, reconstructionId: String, name: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+reconstructionId+".gltf")
//        ModelRenderable.builder()
//            .setSource(
//                activity,
//                Uri.parse(file.toString())
//            )
//            .setIsFilamentGltf(true)
//            .build()
//            .thenAccept { modelRenderable ->
//                addNodeToScene(modelRenderable, Vector3(position.x!!.toFloat(), position.y!!.toFloat(), position.z!!.toFloat()),
//                    Vector3(scale.x!!.toFloat(), scale.y!!.toFloat(), scale.z!!.toFloat()), sceneView, activity) }
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
        val modelNode = ModelNode(
            position = Position(z = 0.0f),
            rotation = Rotation(x = 0.0f)
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
                autoAnimate = false,
                scaleToUnits = 1.0f,
                centerOrigin = io.github.sceneview.math.Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            delay(500)
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

    override fun onDestroy() {
        super.onDestroy()

        cadFile?.let {
            cadFile = null
        }
    }
}