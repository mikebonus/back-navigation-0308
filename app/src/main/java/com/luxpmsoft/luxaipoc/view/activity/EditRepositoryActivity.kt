package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.repositories.RepositoriesModel
import com.luxpmsoft.luxaipoc.model.repositories.request.PhotoRepositoryRequest
import com.luxpmsoft.luxaipoc.model.repositories.request.RepositoryRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.CaptureImage
import kotlinx.android.synthetic.main.activity_edit_repository.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class EditRepositoryActivity: BaseActivity(), CaptureImage.onCaptureImage {
    var repo: RepositoriesModel? = null
    var captureImage: CaptureImage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent))
        window.setGravity(Gravity.CENTER_HORIZONTAL)
        setContentView(R.layout.activity_edit_repository)
        setFinishOnTouchOutside(true)

        init()
        listener()
    }

    fun init() {
//
        val bundle = intent.extras
        bundle?.let {
            repo = Gson().fromJson(it.getString("repository").toString(), RepositoriesModel::class.java)
            repo?.let {
                edtRepositoryName.setText(it.repositoryName)
                it.repositoryPhoto?.let {
                    MyUtils.loadImage(this, (application as LidarApp).prefManager!!.getToken(), it, ivImage,
                       resources.getDrawable(R.drawable.ic_empty))
                }
            }
        }
//
        if (captureImage == null) {
            captureImage = CaptureImage(this, this, getPackageName())
        }
    }

    override fun onResume() {
        super.onResume()
    }

    fun listener() {
        icEdit.setOnClickListener {
            if (MyUtils.allPermissionsGranted(this)) {
                captureImage?.selectImage()
            } else {
                ActivityCompat.requestPermissions(
                    this, MyUtils.REQUIRED_PERMISSIONS, MyUtils.REQUEST_CODE_PERMISSIONS
                )
            }
        }

        btCancel.setOnClickListener {
            finish()
        }

        btCreate.setOnClickListener {
            updateRepository(repo?.repositoryId, edtRepositoryName.text.toString())
            finish()
        }
    }

    fun serviceUpload(bitmap: Bitmap?) {
        MyUtils.showProgress(this, flProgress)
        val builder: MultipartBody.Builder =
            MultipartBody.Builder().setType(MultipartBody.FORM)
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        builder.addFormDataPart(
            "file", "file.jpg",
            RequestBody.create("image/*".toMediaType(), byteArray)
        )

        val requestBody: RequestBody = builder.build()
        APIOpenAirUtils.serviceUpload((application as LidarApp).prefManager!!.getToken(),
            requestBody, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@EditRepositoryActivity, flProgress)
                    val data = result as ResponseBody
                    try {
                        val jsonObject = JSONObject(data.string())
                        var url = ""
                        url = jsonObject.getString("message")
                        updatePhoto(repo?.repositoryId, url)

                    } catch (e:Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@EditRepositoryActivity,
                            "Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@EditRepositoryActivity, flProgress)
                    MyUtils.toastError(this@EditRepositoryActivity, error as ErrorModel)
                }
            })
    }

    fun updatePhoto(repositoryId: String?, url: String) {
        MyUtils.showProgress(this, flProgress)
        var repositoryPhotoAWSlink = PhotoRepositoryRequest()
        repositoryPhotoAWSlink.organizationId = (application as LidarApp).prefManager!!.getOrganizationId()
        repositoryPhotoAWSlink.repositoryPhoto = url
        APIOpenAirUtils.photoUpdateRepository((application as LidarApp).prefManager!!.getToken(),
            repositoryId, repositoryPhotoAWSlink, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@EditRepositoryActivity, flProgress)
                    Toast.makeText(
                        this@EditRepositoryActivity,
                        "Update image successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    url.let {
                        MyUtils.loadImage(this@EditRepositoryActivity, (application as LidarApp).prefManager!!.getToken(), it, ivImage,
                            resources.getDrawable(R.drawable.ic_empty))
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@EditRepositoryActivity, flProgress)
                    MyUtils.toastError(this@EditRepositoryActivity, error as ErrorModel)
                }
            })
    }

    fun updateRepository(repositoryId: String?, repositoryName: String) {
        MyUtils.showProgress(this, flProgress)
        var request = RepositoryRequest()
        request.repositoryName = repositoryName
        request.organizationId = (application as LidarApp).prefManager!!.getOrganizationId()
        APIOpenAirUtils.updateRepository((application as LidarApp).prefManager!!.getToken(),
            repositoryId, request, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@EditRepositoryActivity, flProgress)
                    Toast.makeText(
                        this@EditRepositoryActivity,
                        getString(R.string.update_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@EditRepositoryActivity, flProgress)
                    MyUtils.toastError(this@EditRepositoryActivity, error as ErrorModel)
                }
            })
    }

    override fun onCaptureImageFromUrl(bitmap: Bitmap?) {

    }

    override fun onCaptureImageFromFile(bitmap: Bitmap?) {
        serviceUpload(bitmap)
    }

    override fun onSelectFromGallery(bitmap: Bitmap?, string: String?, mimetype: String?) {
        serviceUpload(bitmap)
    }

    override fun onRecordVideo(uri: Uri?) {

    }

    override fun onSelectFromGalleryMoreImgae(bitmap: Bitmap?) {
    }

    override fun onSelectImage(type: String) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        captureImage?.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        captureImage?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(captureImage != null) {
            captureImage = null
        }
    }
}