package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.MediaController
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModels
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import kotlinx.android.synthetic.main.activity_video_recording_preview.*
import kotlinx.coroutines.launch

class VideoRecordingPreviewActivity: BaseActivity() {
    var nameFolderFile = ""
    var filePath = ""
    var isPlay = false
    var trainedModels: TrainedModels? = null
    var mc:MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_recording_preview)
        MyUtils.setStatusBarTransparentFlagBlack(this)

        init()
        listener()
    }

    fun init() {
        mc = MediaController(this)
        val bundle = intent.extras
        if (bundle != null) {
            nameFolderFile = bundle.getString("pathFile").toString()
            trainedModels = Gson().fromJson(bundle.get("trained").toString(), TrainedModels::class.java)
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

    }

    fun listener() {
        icBack.setOnClickListener {
            DialogFactory.dialogRerecord(
                this@VideoRecordingPreviewActivity,
                object : DialogFactory.Companion.DialogListener.Rerecord {
                    override fun reRecord() {
                        val intent = Intent()
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                })
        }
        btnContinue.setOnClickListener {
            val intent = Intent(this, FramesPreviewActivity::class.java)
            intent.putExtra("pathFile", nameFolderFile)
            intent.putExtra("trained", Gson().toJson(trainedModels))
            startActivity(intent)
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
                mc?.show(0)
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
                mc?.show(0)
            }
        }

        lineRerecord.setOnClickListener {
            DialogFactory.dialogRerecord(
                this@VideoRecordingPreviewActivity,
                object : DialogFactory.Companion.DialogListener.Rerecord {
                    override fun reRecord() {
                        val intent = Intent()
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                })
        }

        videoView.setMediaController(object: MediaController(this) {
            override fun hide() {
                super.hide()
                mc?.show( )
            }

        })
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

        videoView.apply {
            setVideoURI(uri)
            setMediaController(mc)
            requestFocus()
        }
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

}