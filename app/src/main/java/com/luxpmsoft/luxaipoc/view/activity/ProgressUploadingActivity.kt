package com.luxpmsoft.luxaipoc.view.activity

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.arcore.APIARUtils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.ProgressRequestBody
import kotlinx.android.synthetic.main.activity_progress_uploading.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.jcodec.api.android.AndroidSequenceEncoder
import org.jcodec.codecs.png.PNGDecoder
import org.jcodec.common.Preconditions
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.io.SeekableByteChannel
import org.jcodec.common.model.ColorSpace
import org.jcodec.common.model.Picture
import org.jcodec.common.model.Rational
import org.jcodec.scale.ColorUtil
import org.jcodec.scale.Transform
import java.io.*
import java.util.zip.ZipOutputStream
import org.json.JSONObject

class ProgressUploadingActivity: AppCompatActivity(), ProgressRequestBody.UploadCallbacks {
    companion object {
        const val TAG = "ProgressUploadingActivity"
    }
    private var pathRecord: String? = null
    private var lat: String? = ""
    private var long: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_uploading)
        val bundle = intent.extras
        if (bundle != null) {
            pathRecord = bundle.getString("pathFile")
            bundle.getString("lat")?.let {
                lat = bundle.getString("lat")
            }
            bundle.getString("long")?.let {
                long = bundle.getString("long")
            }
            Log.d(TAG, "pathFile = ${pathRecord}")
            requestFileZip(pathRecord)
        }
    }

    fun requestFileZip(pathRecord: String?) {
        tvPreparing.text = "Preparing data for upload"
        val thread = Thread {
            try {
                //source folder include file need zip
//                    val sourceFile =
//                        getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.path + "/Luxpm"

                val fileToZip = File(pathRecord)
                //convert jpg to png and save video
                var files: File? = File(fileToZip.path+"/images")
                if (files != null && files.exists()) {
                    if(files.listFiles() != null && files.listFiles().isNotEmpty()) {
                        val mediaDir = File(pathRecord, "RGB.mp4")
                        if (mediaDir.exists()) {
                            mediaDir.delete()
                        }
                        mediaDir.createNewFile()
                        if (mediaDir.exists()) {
                            val fo: OutputStream = FileOutputStream(mediaDir)
                            fo.close()
                        }

                        var out: SeekableByteChannel? = null
                        try {
                            out = NIOUtils.writableChannel(mediaDir)
                            val encoder = AndroidSequenceEncoder(out, Rational(25, 1))
                            files?.listFiles()?.forEachIndexed { index, element ->
                                try {
                                    val file = File(files.path+"/image".plus(index+1).plus(".jpg"))
                                    if(file.name.toLowerCase().endsWith(".jpg") || file.name.toLowerCase().endsWith("jpeg")) {
                                        val fileInputStream = FileInputStream(file)
                                        val bmp: Bitmap = BitmapFactory.decodeStream(fileInputStream)
                                        encoder.encodeImage(bmp)
                                        fileInputStream.close()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            encoder.finish()
                        } finally {
                            NIOUtils.closeQuietly(out)
                        }

//                val enc =
//                    SequenceEncoder.createWithFps(NIOUtils.writableChannel(mediaDir), Rational(1, 1))
//                val files = File(fileToZip.path+"/images")
//                files.listFiles().forEachIndexed { index, element ->
//                    try {
//                        val file = File(files.path+"/image".plus(index+1).plus(".jpg"))
//                        if(file.name.toLowerCase().endsWith(".jpg") || file.name.toLowerCase().endsWith("jpeg")) {
//                            val fileInputStream = FileInputStream(file)
//                            var bmp: Bitmap = BitmapFactory.decodeStream(fileInputStream)
//                            val out = FileOutputStream(file.path.replace(".jpg", ".png"))
//                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out) //100-best quality
//                            out.close()
//                        }
//                        enc.encodeNativeFrame(decodePNG(File(file.path.replace(".jpg", ".png")), ColorSpace.RGB))
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//                enc.finish()
//                //remove file images
//                for (file in files.listFiles()) {
//                    file.delete()
//                }
//                files.delete()

                        val fos =
                            FileOutputStream(getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!.path +"/"+ fileToZip.name+".zip")
                        val zipOut = ZipOutputStream(fos)
                        MyUtils.zipFile(fileToZip, fileToZip.name, zipOut)
                        zipOut.close()
                        fos.close()
                        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
                            this@ProgressUploadingActivity,
                            OnSuccessListener { instanceIdResult: InstanceIdResult ->
                                val newToken = instanceIdResult.token
                                Log.d(TAG, "Device token: ${newToken}")
                                requestServer(
                                    this@ProgressUploadingActivity, File(
                                        getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!.path +"/"+ fileToZip.name+".zip"
                                    ), newToken, files, fileToZip
                                )
                            })
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "File Images empty",
                                Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "File Images not exist",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

            } catch (e: Exception) {
                e.message
            }
        }
        thread.start()
    }

    fun convertColorSpace(pic: Picture, tgtColor: ColorSpace?): Picture? {
        val tr: Transform = ColorUtil.getTransform(pic.color, tgtColor)
        val res = Picture.create(pic.width, pic.height, tgtColor)
        tr.transform(pic, res)
        return res
    }

    @Throws(IOException::class)
    fun decodePNG(f: File, tgtColor: ColorSpace?): Picture? {
        val picture: Picture = decodePNG0(f)!!
        Preconditions.checkNotNull(picture, "cant decode " + f.path)
        return convertColorSpace(picture, tgtColor)
    }

    @Throws(IOException::class)
    fun decodePNG0(f: File?): Picture? {
        val pngDec = PNGDecoder()
        val buf = NIOUtils.fetchFromFile(f)
        val codecMeta = pngDec.getCodecMeta(buf)
        val pic = Picture.create(
            codecMeta.size.width, codecMeta.size.height,
            ColorSpace.RGB
        )
        return pngDec.decodeFrame(buf, pic.data)
    }

    fun requestServer(activity: Activity, outputFile: File?, firebaseDeviceToken: String, fileImage: File?, fileParent: File?) {
        runOnUiThread {
            //request server
            tvPreparing.text = "Uploading the captured data"
            progressBar.visibility = View.VISIBLE
            val builder: MultipartBody.Builder =
                MultipartBody.Builder().setType(MultipartBody.FORM)
//            builder.addFormDataPart(
//                "user_id", (application as LidarApp).prefManager?.getUserId().toString()
//            )
            val file = ProgressRequestBody(outputFile!!, "application/zip".toMediaType(), this)
            builder.addFormDataPart(
                "zip", outputFile?.name,
                file
            )
            builder.addFormDataPart(
                "deviceName", ""
            )

            if ((application as LidarApp).prefManager!!.getOrganizationId().isNotEmpty()) {
                builder.addFormDataPart(
                    "organizationID", (application as LidarApp).prefManager!!.getOrganizationId()
                )
            }
            builder.addFormDataPart(
                "subscriptionType", (application as LidarApp).prefManager!!.getUserType()
            )

            builder.addFormDataPart(
                "firebaseDeviceToken", firebaseDeviceToken
            )
            lat?.let {
                builder.addFormDataPart(
                    "lat", it
                )
            }
            long?.let {
                builder.addFormDataPart(
                    "lon", it
                )
            }
            val requestBody: RequestBody = builder.build()
            APIOpenAirUtils.sceneReconstruction((application as LidarApp).prefManager!!.getToken(),
                requestBody, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    progressBar.visibility = View.GONE
                    progressBar.progress = 100//remove file images
                    val data = result as ResponseBody
                    if (outputFile.exists()) {
                        outputFile.delete()
                    }

                    if(fileImage != null) {
                        for (file in fileImage?.listFiles()!!) {
                            file.delete()
                        }
                    }

                    if (fileParent != null) {
                        for (fileP in fileParent?.listFiles()!!) {
                            fileP.delete()
                        }
                        fileParent.delete()
                    }

                    try {
                        val jsonObject = JSONObject(data.string())
                        var responseText = ""
                        responseText = jsonObject.getString("message")
                        Toast.makeText(
                            activity, responseText,
                            Toast.LENGTH_SHORT
                        ).show()

                    } catch (e:Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            activity,
                            "Data has been successfully uploaded!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                }

                override fun onError(error: Any?) {
                    if (outputFile.exists()) {
                        outputFile.delete()
                    }
                    MyUtils.toastError(this@ProgressUploadingActivity, error as ErrorModel)
                    finish()
                }
            })
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        progressBar.progress = percentage
    }

    override fun onError() {

    }

    override fun onFinish() {

    }
}