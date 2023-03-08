package com.luxpmsoft.luxaipoc.api

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luxpmsoft.luxaipoc.R
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.TrustManagerFactory

object Utils {
    fun checkCert(context: Context): TrustManagerFactory? {
        val cf = CertificateFactory.getInstance("X.509")
        val caInput = context.resources.openRawResource(R.raw.lidar)
        val ca: Certificate
        ca = try {
            cf.generateCertificate(caInput)
        } finally {
            caInput.close()
        }

        // Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", ca)

        // Create a TrustManager that trusts the CAs in our KeyStore
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(keyStore)
        return tmf
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf<String>(MediaStore.Images.Media.DATA)
            cursor = context.getContentResolver().query(contentUri!!, proj, null, null, null)
            val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor!!.moveToFirst()
            cursor!!.getString(column_index)
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }

    fun getByte(activity: Activity, uri: Uri?): ByteArray {
        val file = File(getRealPathFromURI(activity, uri))
        val bytes = ByteArray(file.length().toInt())
        val buf = BufferedInputStream(FileInputStream(file))
        buf.read(bytes, 0, bytes.size)
        buf.close()
        return bytes
    }

    fun getByteImage(activity: Activity, file: File?): ByteArray {
        val bytes = ByteArray(file!!.length().toInt())
        val buf = BufferedInputStream(FileInputStream(file))
        buf.read(bytes, 0, bytes.size)
        buf.close()
        return bytes
    }

    fun getByte(activity: Activity, file: File?): ByteArray {
        val bytes = ByteArray(file!!.length().toInt())
        val buf = BufferedInputStream(FileInputStream(file))
        buf.read(bytes, 0, bytes.size)
        buf.close()
        return bytes
    }

    fun showProgressBar(view: View, activity: Activity) {
        activity.runOnUiThread(Runnable {
            view.setVisibility(View.VISIBLE)
        })

    }

    fun hideProgressBar(view: View, activity: Activity) {
        activity.runOnUiThread(Runnable { view.setVisibility(View.GONE) })
    }

    fun gridLayoutManager(
        activity: Activity?, recyclerView: RecyclerView,
        count: Int, oriented: Int
    ): GridLayoutManager? {
        val gridLayout = GridLayoutManager(activity, count, oriented, false)
        recyclerView.layoutManager = gridLayout
        recyclerView.setHasFixedSize(true)
        return gridLayout
    }

    fun checkPermission(activity: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.RECORD_AUDIO
        )
                == PackageManager.PERMISSION_GRANTED)
    }

    fun loadImage(
        activity: Activity?,
        url: String?,
        imageView: ImageView
    ) {
        if (url != null && url !== "") {
            Glide.with(activity!!)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        imageView.setImageBitmap(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

//            val myBitmap = getRotatedImage(File(url), BitmapFactory.decodeFile(File(url).toString()))
//            imageView.setImageBitmap(myBitmap)
        }
    }

    fun getRotatedImage(pictureFile: File, myBitmap: Bitmap): Bitmap? {
        val ei = ExifInterface(pictureFile)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
//        val myBitmap: Bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath())

        var rotatedBitmap: Bitmap? = null
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(myBitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(myBitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(myBitmap, 270F)
            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = rotateImage(myBitmap, 90F)
            else -> rotatedBitmap = myBitmap
        }

        return rotatedBitmap
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    /** Milliseconds used for UI animations */
    const val ANIMATION_FAST_MILLIS = 50L
    const val ANIMATION_SLOW_MILLIS = 100L

    /**
     * Simulate a button click, including a small delay while it is being pressed to trigger the
     * appropriate animations.
     */
    fun ImageButton.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
        performClick()
        isPressed = true
        invalidate()
        postDelayed({
            invalidate()
            isPressed = false
        }, delay)
    }

    inline fun View.afterMeasured(crossinline block: () -> Unit) {
        if (measuredWidth > 0 && measuredHeight > 0) {
            block()
        } else {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (measuredWidth > 0 && measuredHeight > 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        block()
                    }
                }
            })
        }
    }
}