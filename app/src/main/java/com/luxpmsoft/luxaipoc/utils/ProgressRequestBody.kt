package com.luxpmsoft.luxaipoc.utils

import android.os.Handler
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import android.os.Looper
import java.io.FileInputStream

class ProgressRequestBody(file: File, content_type: MediaType?, listener: UploadCallbacks) : RequestBody() {
    var mFile: File? = file
    val mPath: String? = null
    var mListener: UploadCallbacks? = listener
    val content_type: MediaType? = content_type

    private val DEFAULT_BUFFER_SIZE = 2048

    interface UploadCallbacks {
        fun onProgressUpdate(percentage: Int)
        fun onError()
        fun onFinish()
    }

    override fun contentType(): MediaType? {
        return content_type
    }

    override fun writeTo(sink: BufferedSink) {
        val fileLength = mFile!!.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val `in` = FileInputStream(mFile)
        var uploaded: Long = 0

        try {
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (`in`.read(buffer).also { read = it } != -1) {

                // update progress on UI thread
                handler.post(ProgressUpdater(uploaded, fileLength, mListener))
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        } finally {
            `in`.close()
        }
    }

    override fun contentLength(): Long {
        return mFile!!.length()
    }

    class ProgressUpdater(private val mUploaded: Long, private val mTotal: Long, val mListener: UploadCallbacks?) :
        Runnable {
        override fun run() {
            mListener?.onProgressUpdate((100 * mUploaded / mTotal).toInt())
        }
    }
}

