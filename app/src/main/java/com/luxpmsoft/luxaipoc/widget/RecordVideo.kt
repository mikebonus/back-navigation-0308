package com.luxpmsoft.luxaipoc.widget

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

class RecordVideo (
    activity: Activity?,
    onCapture: onCaptureImage?,
    packageName: String?
) {
    val TAG = RecordVideo::class.java.simpleName

    private var SELECT_VIDEO:Int = 2

    private val REQUEST_WRITE_EXTERNAL_STORAGE:Int = 7777
    private var mCurrentPhotoPath: String? = null
    //    private var fragment: Fragment? = fragment
    private var activity: Activity? = activity
    private var onCapture: onCaptureImage? = onCapture
    private var packageName: String? = packageName

    //=======================Code chon hinh============================
    //Yeu cau cap quyen
    fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //--------------WRITE_EXTERNAL_STORAGE
            if (activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                activity!!.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                activity!!.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) {
                if (activity!!.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.e(TAG, "Permission isn't granted")
                } else if (activity!!.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Log.e(TAG, "Permission isn't granted")
                } else if (activity!!.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)){
                    Log.e(TAG, "Permisson don't granted and dont show dialog again")
                }
                activity!!.requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    ), REQUEST_WRITE_EXTERNAL_STORAGE
                )
            } else {
                //Exits Permision
            }
        } else {
            //API small
        }
    }

    // Khi nguoi dung yeu cau cap quyen(cho phep hoac tu choi).
    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            activity!!,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
//                        takeVideoFromCamera()
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "Not grand",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
            }
        }
    }

    /*Lang nghe ket qua tra ve*/
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode === SELECT_VIDEO) {
                val contentURI: Uri = data!!.data!!
                val recordedVideoPath: String = contentURI.path!!
                try {
                    onCapture!!.onRecordVideo(contentURI)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                } finally {
                }
//                    videoView.setVideoURI(contentURI)
//                    videoView.requestFocus()
//                    videoView.start()
            }
        }
    }

    fun takeVideoFromCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15)
        activity!!.startActivityForResult(intent, SELECT_VIDEO)
    }

    interface onCaptureImage {
        fun onRecordVideo(uri: Uri?)
    }
}