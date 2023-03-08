package com.luxpmsoft.luxaipoc.widget

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.utils.MyUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class CaptureImage(
    activity: Activity?,
    onCapture: onCaptureImage?,
    packageName: String?,
    val fragment: Fragment? = null,
) {
    val TAG = CaptureImage::class.java.simpleName

    private val REQUEST_CAMERA:Int = 0
    private var SELECT_FILE:Int = 1
    private var SELECT_VIDEO:Int = 2

    private val REQUEST_WRITE_EXTERNAL_STORAGE:Int = 7777
    private var mCurrentPhotoPath: String? = null
    //    private var fragment: Fragment? = fragment
    private var activity: Activity? = activity
    private var onCapture: onCaptureImage? = onCapture
    private var packageName: String? = packageName
    private var type: String? = null

    //=======================Code chon hinh============================
    //Yeu cau cap quyen
    fun initPermission(ty : String) {
        type = ty
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //--------------WRITE_EXTERNAL_STORAGE
            if (activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                activity!!.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                if (activity!!.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.e(TAG, "Permission isn't granted")
                } else if (activity!!.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Log.e(TAG, "Permission isn't granted")
                } else {
                    Log.e(TAG, "Permisson don't granted and dont show dialog again")
                }
                activity!!.requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
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
                        if (type.equals("CAMERA")) {
                            cameraIntent()
                        } else {
                            galleryIntent()
                        }
                        Log.e(TAG, "Permision is Granted")
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "Permission isn't granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
            }
        }
    }

    /* Chon hinh tu thu vien hoac camera*/
    fun selectImage() {
        try {
//            galleryIntent()
            val items = arrayOf<CharSequence>(
                activity!!.getString(R.string.str_TakePhoto),
                activity!!.getString(R.string.str_ChoosefromLibrary),
                activity!!.getString(R.string.str_Cancel)
            )
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle(R.string.str_AddPhoto)
            builder.setItems(items) { dialog, item ->
                val result: Boolean = MyUtils.allPermissionsGranted(activity!!)
                if (items[item] == activity!!.getString(R.string.str_TakePhoto)) {
                    if (result) cameraIntent()
                } else if (items[item] == activity!!.getString(R.string.str_ChoosefromLibrary)) {
                    if (result) galleryIntent()
                } else if (items[item] == activity!!.getString(R.string.str_Cancel)) {
                    dialog.dismiss()
                }
            }
            builder.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*Lang nghe ket qua tra ve*/
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) onSelectFromGalleryResult(data) else if (requestCode == REQUEST_CAMERA) {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val f = File(mCurrentPhotoPath)
                val contentUri = Uri.fromFile(f)
                mediaScanIntent.data = contentUri
                activity!!.sendBroadcast(mediaScanIntent)
                onCaptureImageResultFile(contentUri)
            } else {
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
    }

    /*Goi Inten mo camera tu he dieu hanh*/
    fun cameraIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity!!.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    val photoURI = FileProvider.getUriForFile(
                        activity!!,
                        packageName!!, photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    activity!!.startActivityForResult(takePictureIntent, REQUEST_CAMERA)
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    fun takeVideoFromCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        activity!!.startActivityForResult(intent, SELECT_VIDEO)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an Paths file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        //File storageDir = new File(Environment.getExternalStorageDirectory(), "My Equip");
        val storageDir = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir!!.exists()) {
            storageDir.mkdirs()
        }
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    /*Mở thư viện ảnh*/
    fun galleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }*/intent.action = Intent.ACTION_GET_CONTENT
        if (fragment != null) {
            fragment!!.startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE)
        } else {
            activity!!.startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE)
        }
    }

    /*Ham tra ve khi chup hinh*/
    private fun onCaptureImageResult(data: Intent) {
        val thumbnail = data.extras!!["data"] as Bitmap?
        if (thumbnail != null) {
            onCapture!!.onCaptureImageFromUrl(thumbnail)
        }
    }

    /*Ham tra ve bitmap khi chup hinh tu Uri*/
    private fun onCaptureImageResultFile(contentUri: Uri) {
        var bm: Bitmap? = null
        try {
            //if (checkSizeImg(selectedImage) <= 5) {
            val mImagePath = contentUri.path
            val options = BitmapFactory.Options()
            options.inSampleSize = 2
            options.inScaled = false
            val stream = activity!!.contentResolver.openInputStream(contentUri)
            bm = BitmapFactory.decodeStream(stream, null, options)
            stream!!.close()

            if (bm != null) {
                //orientation
                var rotate = 0
                val exif = ExifInterface(
                    mImagePath!!
                )
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
                }
                val matrix = Matrix()
                matrix.postRotate(rotate.toFloat())
//                bm = Bitmap.createBitmap(
//                    bm, 0, 0,
//                    (bm.width * 0.75).toInt(), (bm.height * 0.75).toInt(), matrix, true
//                )
                if (bm != null) {
                    onCapture!!.onCaptureImageFromFile(bm)
                }
            }
            /*} else {
                DialogFactory.createMessageDialog(activity, activity.getString(R.string.txtSizeImg));
            }*/
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /*Ham tra ve bitmap khi chon hinh tu thu vien*/
    private fun onSelectFromGalleryResult(data: Intent?) {
        var bm: Bitmap? = null
        var fileName : String? = null
        var mimetype : String? = null
        if (data != null) {
            try {
                val selectedImage = data.data
                Log.e("kích thước 5Mb", (5 * 1024 * 1024).toString())
                //if (checkSizeImg(selectedImage) <= 5) {
                mimetype = MyUtils.GetMimeType(activity!!, selectedImage)
                val mImagePath = selectedImage?.path
                fileName = mImagePath?.substring(mImagePath.lastIndexOf("/")+1)
                val options = BitmapFactory.Options()
                options.inSampleSize = MyUtils.inSampleSize(mImagePath)
                options.inScaled = false
                val stream = activity!!.contentResolver.openInputStream(selectedImage!!)
                bm = BitmapFactory.decodeStream(stream, null, options)
                stream!!.close()
                if (bm != null) {
                    //orientation
                    var rotate = 0
                    val exif = mImagePath?.let { ExifInterface(it) }
                    val orientation = exif?.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
                    }
                    val matrix = Matrix()
                    matrix.postRotate(rotate.toFloat())
                    bm = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
                }
                /*} else {
                    DialogFactory.createMessageDialog(activity, activity.getString(R.string.txtSizeImg));
                }*/
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (bm != null) {
            onCapture!!.onSelectFromGallery(bm, fileName, mimetype)
        }
    }

    /*Ham tra ve danh sach bitmap khi chon nhieu hinh tu thu vien*/
    private fun onSelectListFromGalleryResult(data: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && data.clipData != null) {
            val numberOfImages = data.clipData!!.itemCount
            for (i in 0 until numberOfImages) {
                try {
                    val imageData = ImageData()
                    imageData.uri = data.clipData!!.getItemAt(i).uri
                    //if (checkSizeImg(imageData.uri) <= 5) {
                    val mImagePath = getRealPathFromDocumentUri(activity, imageData.uri)
                    val image_stream = activity!!.contentResolver.openInputStream(imageData.uri!!)
                    val options = BitmapFactory.Options()
                    options.inSampleSize = MyUtils.inSampleSize(mImagePath)
                    options.inScaled = false
                    var bitmap = BitmapFactory.decodeStream(image_stream, null, options)
                    image_stream!!.close()

                    //orientation
                    var rotate = 0
                    val exif = ExifInterface(mImagePath)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
                    }
                    val matrix = Matrix()
                    matrix.postRotate(rotate.toFloat())
                    bitmap = Bitmap.createBitmap(
                        bitmap!!,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        matrix,
                        true
                    )
                    imageData.icon = bitmap

                    //--------------------------------
                    /*if (imageData.icon != null) {
                            listObject.add(new GridViewModel(listObject.size(), typeListImageS2, imageData.icon, false, true));
                        }*/
                    /*} else {
                        DialogFactory.createMessageDialog(activity, activity.getString(R.string.txtSizeImg));
                    }*/
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getRealPathFromDocumentUri(context: Context?, uri: Uri?): String {
        var filePath = ""
        val p = Pattern.compile("(\\d+)$")
        val m = p.matcher(uri.toString())
        if (!m.find()) {
            Log.e("ABCD", "ID for requested Paths not found: " + uri.toString())
            return filePath
        }
        val imgId = m.group()
        val column = arrayOf(MediaStore.Images.Media.DATA)
        val sel = MediaStore.Images.Media._ID + "=?"
        val cursor = context!!.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            column, sel, arrayOf(imgId), null
        )
        val columnIndex = cursor!!.getColumnIndex(column[0])
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex)
        }
        cursor.close()
        return filePath
    }

    /*Check kích thước của hình lấy từ Uri*/
    private fun checkSizeImg(selectedImage: Uri): Long {
        var dataSize = 0
        val scheme = selectedImage.scheme
        println("Scheme type $scheme")
        if (scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                val fileInputStream =
                    activity!!.applicationContext.contentResolver.openInputStream(selectedImage)
                dataSize = fileInputStream!!.available()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.e("File size in MB", (dataSize / 1024 / 1024).toString())
            return (dataSize / 1024 / 1024).toLong()
        } else if (scheme == ContentResolver.SCHEME_FILE) {
            var f: File? = null
            val path = selectedImage.path
            try {
                f = File(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.e("File size in MB", (f!!.length() / 1024 / 1024).toString())
            return f.length() / 1024 / 1024
        }
        return 0
    }

    //====================================End code chon hinh=============================
    interface onCaptureImage {
        fun onCaptureImageFromUrl(bitmap: Bitmap?)
        fun onCaptureImageFromFile(bitmap: Bitmap?)
        fun onSelectFromGallery(bitmap: Bitmap?, string: String?, mimetype: String?)
        fun onRecordVideo(uri: Uri?)
        fun onSelectFromGalleryMoreImgae(bitmap: Bitmap?)
        fun onSelectImage(type : String)
    }
}