package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.URLUtil
import android.widget.MediaController
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.workout.ExerciseSessionData
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_workout_video.*
import java.io.File

class WorkoutVideoActivity: BaseActivity() {
    var workout: ExerciseSessionData? = null
    var videoUrl: Uri? = null
    var isPlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_video)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            workout = Gson().fromJson(bundle.getString("data"), ExerciseSessionData::class.java)
        }

        workout?.let {
            tvKcal.text = it.calories.toString().plus(" kcal")
            tvReps.text = it.reps.toString().plus(" reps")
            tvTime.text = MyUtils.convertDateTime(workout?.createdAt.toString())

            MyUtils.showProgress(this, flProgress)
            downloadModel(BuildConfig.URL_WORKOUT_VIDEO+it.videoUrl)
        }
    }

    fun listener() {
        icBack.setOnClickListener {
            videoUrl?.let {
                if (File(videoUrl?.path).exists()) {
                    File(videoUrl?.path).delete()
                }
            }

            finish()
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

    @SuppressLint("Range")
    fun downloadModel(url: String) {
        val downloadManager: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
        val title = URLUtil.guessFileName(url, null, "video/mp4")
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
                        // File received
                        val manager =getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        videoUrl = manager.getUriForDownloadedFile(downloadId)
                        val mc = MediaController(this)
                        videoView.apply {
                            setVideoURI(videoUrl)
                            setMediaController(mc)
                            requestFocus()
                        }
                        videoView.setMediaController(mc)
                        mc.visibility = View.GONE
                    }
                }
                cursor.close()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoUrl?.let {
            if (File(videoUrl?.path).exists()) {
                File(videoUrl?.path).delete()
            }
        }
    }
}