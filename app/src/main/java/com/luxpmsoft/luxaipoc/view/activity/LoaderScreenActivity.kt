package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.defectdetect.APIDefectDetectUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.defect_detect.Annotation
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModels
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.ProgressRequestBody
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import kotlinx.android.synthetic.main.activity_loader_screen.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.*
import kotlin.math.round

class LoaderScreenActivity: BaseActivity(), ProgressRequestBody.UploadCallbacks {
    var trainedModels: TrainedModels? = null
    private var path = ""
    var file: File? = null
    var index = 0
    var isProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader_screen)

        init()
    }

    fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            trainedModels = Gson().fromJson(bundle.get("trained").toString(), TrainedModels::class.java)
            path = bundle.get("path").toString()
            file = File(path)
            increaseDataset()
        }
    }

    fun generateSession() {
        val builder: MultipartBody.Builder =
            MultipartBody.Builder().setType(MultipartBody.FORM)

        builder.addFormDataPart(
            "user_id", "103"
        )

        val requestBody: RequestBody = builder.build()
        APIDefectDetectUtils.generateSession((application as LidarApp).prefManager!!.getToken(), requestBody, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as ResponseBody

            }

            override fun onError(error: Any?) {
                MyUtils.toastError(this@LoaderScreenActivity, error as ErrorModel)
            }
        })
    }

    fun increaseDataset() {
        runOnUiThread {
            progressBar.progress = 0
            var max = 0
            for(item in file?.listFiles()!!) {
                if (item.name.toLowerCase().endsWith(".jpeg")) {
                    max += 1
                }
            }
            progressBar.max = max
            progressBar.visibility = View.VISIBLE
            val builder: MultipartBody.Builder =
                MultipartBody.Builder().setType(MultipartBody.FORM)
            for(item in file?.listFiles()!!) {
                if (item.name.toLowerCase().endsWith(".jpeg")) {
                    val file = ProgressRequestBody(item, "application/image".toMediaType(), this)
                    builder.addFormDataPart(
                        "files", item.name,
                        file
                    )
                }
            }

            var annotate: ArrayList<Annotation> = ArrayList()
            for (text in file?.listFiles()!!) {
                if (text.name.lowercase().endsWith(".txt")) {
                    try {
                        val inputStream: InputStream = File(text.path).inputStream()
                        inputStream?.let {
                            val inputStreamReader = InputStreamReader(inputStream)
                            val bufferedReader = BufferedReader(inputStreamReader)
                            var receiveString: String? = ""
                            val stringBuilder = StringBuilder()
                            while (bufferedReader.readLine().also { receiveString = it } != null) {
                                stringBuilder.append(receiveString)
                            }
                            inputStream.close()
                            val strs = stringBuilder.toString().split(";")
                            for (main in strs) {
                                try {
                                    val sub = main.split(" ")
                                    if (sub.isNotEmpty()) {
                                        annotate.add(Annotation(text.name.lowercase().replace(".txt", ".jpeg"),
                                            round(sub[0].toFloat()),
                                            sub[1].toFloat(), sub[2].toFloat(), sub[3].toFloat(), sub[4].toFloat()))
                                    }
                                } catch (e: IndexOutOfBoundsException) {
                                    e.printStackTrace()
                                } catch (e : NumberFormatException) {
                                    e.printStackTrace()
                                }
                            }
                            Log.e("FOLDER", Gson().toJson(annotate))
                        }
                    } catch (e: FileNotFoundException) {
                        Log.e("login activity", "File not found: $e")
                    } catch (e: IOException) {
                        Log.e("login activity", "Can not read file: $e")
                    }
                }
            }

            builder.addFormDataPart(
                "anotations", Gson().toJson(annotate)
            )

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
                "retrain_flag", "true"
            )

            val requestBody: RequestBody = builder.build()
            APIDefectDetectUtils.increaseDataset((application as LidarApp).prefManager!!.getToken(), requestBody, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    val data = result as ResponseBody
                    progressBar.visibility = View.GONE
                    progressBar.progress = 100//remove file images
                    retrainProcess()
                }

                override fun onError(error: Any?) {
                    MyUtils.toastError(this@LoaderScreenActivity, error as ErrorModel)
                    finish()
                }
            })
        }
    }

    fun retrainProcess() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
            this,
            OnSuccessListener { instanceIdResult: InstanceIdResult ->
                val newToken = instanceIdResult.token
                val builder: MultipartBody.Builder =
                    MultipartBody.Builder().setType(MultipartBody.FORM)
                builder.addFormDataPart(
                    "firebase_device_token", newToken
                )

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

                val requestBody: RequestBody = builder.build()
                APIDefectDetectUtils.retrainProcess((application as LidarApp).prefManager!!.getToken(), requestBody, object : APIInterface.onDelegate {
                    override fun onSuccess(result: Any?) {
                        val data = result as ResponseBody
                        removeFile()
                        lnProgress.visibility = View.GONE
                        DialogFactory.dialogRetrainSuccess(
                            this@LoaderScreenActivity,
                            object : DialogFactory.Companion.DialogListener.Retrain {
                                override fun reTrain() {
                                    startHome()
                                }
                            }, resources.getString(R.string.str_retrain_success))
                    }

                    override fun onError(error: Any?) {
                        MyUtils.toastError(this@LoaderScreenActivity, error as ErrorModel)
                        removeFile()
                        startHome()
                    }
                })
            })
    }

    fun startHome() {
        val intent = Intent(this@LoaderScreenActivity, HomeOrganizationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    fun removeFile() {
        if(file != null) {
            for (f in file?.listFiles()!!) {
                f.delete()
            }
            file?.delete()
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        if (isProgress && percentage == 0) {
            isProgress = false
        }

        if (!isProgress && percentage == 99) {
            isProgress = true
            index++
            progressBar.progress = index
        }
    }

    override fun onError() {

    }

    override fun onFinish() {

    }
}