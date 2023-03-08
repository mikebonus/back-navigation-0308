package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.location.Location
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.video.APIVideoUtils
import com.luxpmsoft.luxaipoc.api.workout.APIWorkoutUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ExerciseCategory
import com.luxpmsoft.luxaipoc.model.defect_detect.ExerciseCategoryResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.ProgressRequestBody
import com.luxpmsoft.luxaipoc.widget.CurrentLocation
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import kotlinx.android.synthetic.main.activity_preview_video.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.File

class PreviewVideoActivity: BaseActivity(), ProgressRequestBody.UploadCallbacks, CurrentLocation.OnLocationResolved {
    companion object {
        const val TAG = "PreviewVideoActivity"
    }
    var nameFolderFile = ""
    var isProgress = false
    var filePath = ""
    var from = ""
    var currentLocation : CurrentLocation? = null
    var mFusedLocationClient: FusedLocationProviderClient? = null
    var isPlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_video)
        MyUtils.setStatusBarTransparentFlagBlack(this)

        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            nameFolderFile = bundle.getString("pathFile").toString()
            from = bundle.getString("from").toString()
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            showVideo(Uri.parse(nameFolderFile))
        } else {
            // force MediaScanner to re-scan the media file.
            val path = getAbsolutePathFromUri(Uri.parse(nameFolderFile)) ?: return
            MediaScannerConnection.scanFile(
                this, arrayOf(path), null
            ) { _, uri ->
                // playback video on main thread with VideoView
                if (uri != null) {
                    lifecycleScope.launch {
                        showVideo(uri)
                    }
                }
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        currentLocation = CurrentLocation(this, mFusedLocationClient, this)

        icBack.setOnClickListener {
            deleteFile()
            finish()
        }

        btnUploadVideo.setOnClickListener {
            if (from == "workout") {
                getExerciseCategory()
            } else {
                currentLocation?.getLastLocation()
            }
        }

        imgStartStop.setOnClickListener {
            if (isPlay) {
                isPlay = false
                imgStartStop.visibility = View.VISIBLE
                videoView.pause()
            } else {
                isPlay = true
                imgStartStop.visibility = View.GONE
                videoView.start()
            }
        }

        videoView.setOnClickListener {
            if (isPlay) {
                isPlay = false
                imgStartStop.visibility = View.VISIBLE
                videoView.pause()
            } else {
                isPlay = true
                imgStartStop.visibility = View.GONE
                videoView.start()
            }
        }
    }

    fun uploadVideo(location: Location?) {
        if (File(filePath).exists()) {
            progressBar1.progress = 0
            progressBar1.max = 100
            lnUpload.visibility = View.GONE
            lnProgress.visibility = View.VISIBLE
            requestMulti(File(filePath), location)
        }
    }
    //upload photo
    fun requestMulti(file: File, location: Location?) {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
            this,
            OnSuccessListener { instanceIdResult: InstanceIdResult ->
                val newToken = instanceIdResult.token
                val builder: MultipartBody.Builder =
                    MultipartBody.Builder().setType(MultipartBody.FORM)
                val file = ProgressRequestBody(file, "application/video".toMediaType(), this)
                builder.addFormDataPart(
                    "files", "RGB.mp4",
                    file
                )

                builder.addFormDataPart(
                    "x_access_token", (application as LidarApp).prefManager!!.getToken()
                )

                if ((application as LidarApp).prefManager!!.getOrganizationId().isNotEmpty()) {
                    builder.addFormDataPart(
                        "organization_id", (application as LidarApp).prefManager!!.getOrganizationId()
                    )
                }

                builder.addFormDataPart(
                    "user_id", (application as LidarApp).prefManager!!.getUserId()
                )

                builder.addFormDataPart(
                    "firebase_device_token", newToken
                )

                location?.latitude?.let {
                    builder.addFormDataPart(
                        "lat", it.toString()
                    )
                }

                location?.longitude?.let {
                    builder.addFormDataPart(
                        "lon", it.toString()
                    )
                }

                val requestBody: RequestBody = builder.build()
                APIVideoUtils.uploadMutiVideo(requestBody, object : APIInterface.onDelegate {
                    override fun onSuccess(result: Any?) {
                        deleteFile()
                        val data = result as ResponseBody
                        try {
                            val jsonObject = JSONObject(data.string())
                            var responseText = ""
                            responseText = jsonObject.getString("msg")
                            Toast.makeText(
                                this@PreviewVideoActivity, responseText,
                                Toast.LENGTH_SHORT
                            ).show()

                        } catch (e:Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@PreviewVideoActivity,
                                "Data has been successfully uploaded!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        startHome()
                    }

                    override fun onError(error: Any?) {
                        Toast.makeText(
                            this@PreviewVideoActivity,
                            "File Uploaded Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            })
    }

    fun requestWorkoutVideo(file: File, categoryName: String, id: String?) {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
            this,
            OnSuccessListener { instanceIdResult: InstanceIdResult ->
                val newToken = instanceIdResult.token
                val builder: MultipartBody.Builder =
                    MultipartBody.Builder().setType(MultipartBody.FORM)
                val file = ProgressRequestBody(file, "application/video".toMediaType(), this)
                builder.addFormDataPart(
                    "video", "video.mp4",
                    file
                )

                builder.addFormDataPart(
                    "workoutCategoryName", categoryName
                )

                if (id != null) {
                    builder.addFormDataPart(
                        "isCategoryNew", "false"
                    )
                } else {
                    builder.addFormDataPart(
                        "isCategoryNew", "true"
                    )
                }

                id?.let {
                    builder.addFormDataPart(
                        "categoryId", it
                    )
                }

                builder.addFormDataPart(
                    "firebaseDeviceToken", newToken
                )

                val requestBody: RequestBody = builder.build()
                APIWorkoutUtils.uploadVideo((application as LidarApp).prefManager!!.getToken(), requestBody, object : APIInterface.onDelegate {
                    override fun onSuccess(result: Any?) {
                        deleteFile()
                        val data = result as ResponseBody
                        try {
                            val jsonObject = JSONObject(data.string())
                            var responseText = ""
                            responseText = jsonObject.getString("message")
                            Toast.makeText(
                                this@PreviewVideoActivity, responseText,
                                Toast.LENGTH_SHORT
                            ).show()

                        } catch (e:Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@PreviewVideoActivity,
                                "Data has been successfully uploaded!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        startHome()
                    }

                    override fun onError(error: Any?) {
                        Toast.makeText(
                            this@PreviewVideoActivity,
                            "File Uploaded Error",
                            Toast.LENGTH_SHORT
                        ).show()
                        startHome()
                    }
                })
            })
    }

    fun getExerciseCategory() {
        APIWorkoutUtils.getExerciseCategory((application as LidarApp).prefManager!!.getToken(), 1000, 1, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as ExerciseCategoryResponse
                var category: ArrayList<ExerciseCategory> = ArrayList()
                data?.body?.categories?.let {
                    category.addAll(it)
                }
                DialogFactory.dialogInputCategoryWorkout(
                    this@PreviewVideoActivity,
                    object : DialogFactory.Companion.DialogListener.Workout {
                        override fun categoryName(name: String, id: String?) {
                            if (File(filePath).exists()) {
                                progressBar1.progress = 0
                                progressBar1.max = 100
                                lnUpload.visibility = View.GONE
                                lnProgress.visibility = View.VISIBLE
                                requestWorkoutVideo(File(filePath), name, id)
                            }
                        }
                    }, category)
            }

            override fun onError(error: Any?) {
                MyUtils.toastError(this@PreviewVideoActivity, error as ErrorModel)
            }
        })
    }

    fun startHome() {
        val intent : Intent
        if ((application as LidarApp).prefManager!!.getOrganizationId().isNotEmpty()) {
            intent = Intent(this, HomeOrganizationActivity::class.java)
        } else {
            intent = Intent(this, HomeActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun showVideo(uri : Uri) {
        val fileSize = getFileSizeFromUri(uri)
        if (fileSize == null || fileSize <= 0) {
            Log.e("VideoViewerFragment", "Failed to get recorded file size, could not be played!")
            return
        }

        filePath = getAbsolutePathFromUri(uri) ?: return
        val fileInfo = "FileSize: $fileSize\n $filePath"
        Log.i("VideoViewerFragment", fileInfo)

        val mc = MediaController(this)
        videoView.apply {
            setVideoURI(uri)
            setMediaController(mc)
            requestFocus()
        }
        mc.visibility = View.GONE
    }

    /**
     * A helper function to get the captured file location.
     */
    private fun getAbsolutePathFromUri(contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = contentResolver
                .query(contentUri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
            if (cursor == null) {
                return null
            }
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } catch (e: RuntimeException) {
            Log.e("VideoViewerFragment", String.format(
                "Failed in getting absolute path for Uri %s with Exception %s",
                contentUri.toString(), e.toString()
            )
            )
            null
        } finally {
            cursor?.close()
        }
    }

    /**
     * A helper function to retrieve the captured file size.
     */
    private fun getFileSizeFromUri(contentUri: Uri): Long? {
        val cursor = contentResolver
            .query(contentUri, null, null, null, null)
            ?: return null

        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()

        cursor.use {
            return it.getLong(sizeIndex)
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        progressBar1.progress = percentage
    }

    override fun onError() {

    }

    override fun onFinish() {

    }

    fun deleteFile() {
        try {
            if (File(filePath).exists()) {
                File(filePath).delete()
            }
        } catch (e: Exception) {
            e.message
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        currentLocation?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onLocationResolved(location: Location?) {
        uploadVideo(location)
    }
}