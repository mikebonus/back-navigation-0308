package com.luxpmsoft.luxaipoc.view.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.ListImageAdapter
import java.io.File
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.adapter.DraftImageAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.APIUtils
import com.luxpmsoft.luxaipoc.api.ConstantAPI
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.model.FinishUploadPhotoResponse
import com.luxpmsoft.luxaipoc.model.listimage.DraftImageModel
import com.luxpmsoft.luxaipoc.model.listimage.ListImageModel
import com.luxpmsoft.luxaipoc.utils.ProgressRequestBody
import kotlinx.android.synthetic.main.activity_list_image.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.*

class ListImageActivity: AppCompatActivity(), ProgressRequestBody.UploadCallbacks {
    var adapter: ListImageAdapter? = null
    var listImage: ArrayList<ListImageModel>? = ArrayList()
    var draftAdapter: DraftImageAdapter? = null
    var draftImage: ArrayList<DraftImageModel>? = ArrayList()
    var nameFolderFile = ""
    private var mProgressDialog: ProgressDialog? = null
    private var mSessionId: String? = null
    var index = 0
    var folder: File? = null
    var isProgress = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_image)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog?.setMessage("Please wait ...")
        init()

    }

    fun init() {
        tvDeleteAll.setOnClickListener {
            deleteAllFile()
            startHome()
        }

        btnUpload.setOnClickListener {
            if(listImage!!.size > 0) {
                progressBar1.progress = 0
                progressBar1.max = listImage!!.size
                lnUpload.visibility = View.GONE
                lnProgress.visibility = View.VISIBLE
                requestMulti(listImage)
            }
        }

//        Utils.gridLayoutManager(this, grvDraft, 3, GridLayoutManager.VERTICAL)
//        draftAdapter = DraftImageAdapter(this, R.layout.item_draft_image, draftImage!!)
//        grvDraft.adapter = draftAdapter

        Utils.gridLayoutManager(this, grvListImage, 3, GridLayoutManager.VERTICAL)
        adapter = ListImageAdapter(this, R.layout.item_image, listImage!!, object :
            ListImageAdapter.IAdapterClickListener {
            override fun onRemove(position: Int) {
                try {
                    listImage?.removeAt(position)
                    adapter?.notifyDataSetChanged()
                    folder?.listFiles()?.sorted()?.get(position)?.delete()
                    tvTotal.setText("IMAGES(" + listImage?.size + ")")
                } catch (e:Exception) {
                    e.message
                }
            }
        })
        grvListImage.adapter = adapter

        val bundle = intent.extras
        if (bundle != null) {
            nameFolderFile = bundle.getString("pathFile").toString()
        }

        //get list image from storage
        folder = File(nameFolderFile)
        if (!folder?.exists()!!) folder?.mkdir()
        for (file in folder?.listFiles()?.sorted()!!) {
            val filename = file.name.toLowerCase()
            if (filename.endsWith(".jpg") || filename.endsWith("jpeg")) {
                val image = ListImageModel()
                image.url = folder?.path+"/"+filename
                image.name = filename
                listImage?.add(image)
            }
        }


        adapter?.notifyDataSetChanged()
        tvTotal.setText("IMAGES("+listImage?.size+")")
        progressBar.progress = 0  // Main Progress
        progressBar.secondaryProgress = listImage!!.size // Secondary Progress
        progressBar.max = listImage!!.size
//        val getAllFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path)
//        for (file in getAllFile.listFiles()) {
//            val filename = file.name.toLowerCase()
//            if(draftImage!!.size < 12 && filename.contains("lidarapp_") && filename != "LidarApp_"+nameFolderFile) {
//                val fileFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filename)
//                if(fileFolder.exists() && fileFolder.listFiles().isNotEmpty()) {
//                    for (file in fileFolder.listFiles()) {
//                        val filename = file.name.toLowerCase()
//                        if (filename.endsWith(".jpg") || filename.endsWith("jpeg")) {
//                            val image = DraftImageModel()
//                            image.url = fileFolder.path+"/"+file.name
//                            image.total = fileFolder.listFiles().size
//                            draftImage?.add(image)
//                            break
//                        }
//                    }
//                }
//            }
//        }
//
//        Log.e("OKOK", draftImage?.size.toString())
//        draftAdapter?.notifyDataSetChanged()

        icClose.setOnClickListener {
            deleteAllFile()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    //upload photo
    fun requestMulti(listImage: ArrayList<ListImageModel>?) {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
            this,
            OnSuccessListener { instanceIdResult: InstanceIdResult ->
                val newToken = instanceIdResult.token
                val builder: MultipartBody.Builder =
                    MultipartBody.Builder().setType(MultipartBody.FORM)
                for(item in listImage!!) {
                    val file = ProgressRequestBody(File(item.url), "application/image".toMediaType(), this)
                    builder.addFormDataPart(
                        "files", item.name,
                        file
                    )
                }

                builder.addFormDataPart(
                    "x_access_token", (application as LidarApp).prefManager!!.getToken()
                )

                if ((application as LidarApp).prefManager!!.getOrganizationId().isNotEmpty()) {
                    builder.addFormDataPart(
                        "organization_id", (application as LidarApp).prefManager!!.getOrganizationId()
                    )
                }

                builder.addFormDataPart(
                    "user_id", ConstantAPI.USER_ID
                )

                builder.addFormDataPart(
                    "firebase_device_token", newToken
                )

                val requestBody: RequestBody = builder.build()
                APIUtils.uploadMutiPhoto(requestBody, object : APIInterface.onDelegate {
                    override fun onSuccess(result: Any?) {
                        if(index == listImage?.size) {
                            deleteAllFile()
                            val builder = AlertDialog.Builder(this@ListImageActivity)
                            builder.setMessage("Upload Successfully ")

                            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                                dialog.dismiss()
                                startHome()
                            }

                            builder.show()
                        }
                    }

                    override fun onError(error: Any?) {

                        Toast.makeText(
                            this@ListImageActivity,
                            "File Uploaded Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            })
    }

    //upload photo
    fun requestServer(mSessionId: String?, name: String?, outputFile: File?) {
        //request server
        Log.e("BYTE", Utils.getByteImage(this, outputFile).toString())
        val builder: MultipartBody.Builder =
            MultipartBody.Builder().setType(MultipartBody.FORM)
        builder.addFormDataPart(
            "file", name,
            RequestBody.create("image/*".toMediaType(), outputFile?.readBytes()!!)
        )

        builder.addFormDataPart(
            "session_id", mSessionId.toString()
        )

        builder.addFormDataPart(
            "user_id", ConstantAPI.USER_ID
        )

        val requestBody: RequestBody = builder.build()
        APIUtils.uploadPhoto(requestBody, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                index++
                if(index == listImage?.size) {
//                    mProgressDialog?.hide()
                    sendFinishPhotoUpload()
                }

                tvUpload.text = index.toString()
                progressBar.progress = index
            }

            override fun onError(error: Any?) {
                index++
                if(index == listImage?.size) {
//                    mProgressDialog?.hide()
                }
                Toast.makeText(
                    this@ListImageActivity,
                    "File Uploaded Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun sendFinishPhotoUpload() {
//        mProgressDialog?.show()
        APIUtils.finishPhotoUpload(ConstantAPI.USER_ID, mSessionId!!, object: APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
//                mProgressDialog?.hide()
                val response = result as FinishUploadPhotoResponse
                deleteAllFile()

                handleFinishPhotoUploadSuccessfully(response.msg.toString(), response.session_id.toString())
            }

            override fun onError(error: Any?) {
//                mProgressDialog?.hide()

                Toast.makeText(
                    this@ListImageActivity,
                    "Error",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    fun handleFinishPhotoUploadSuccessfully(msg: String, sessionId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("SessionId: " + sessionId + "\n" + msg)

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
            startHome()
        }

        builder.show()
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

    fun deleteAllFile() {
        try {
            if (folder?.exists()!!) {
                for (file in folder?.listFiles()?.sorted()!!) {
                    file.delete()
                }
                folder?.delete()
            }
        } catch (e: Exception) {
            e.message
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        if (isProgress && percentage == 0) {
            isProgress = false
        }

        if (!isProgress && percentage == 99) {
            isProgress = true
            index++
            tvUpload.text = index.toString()
            progressBar1.progress = index
        }
    }

    override fun onError() {

    }

    override fun onFinish() {
    }
}