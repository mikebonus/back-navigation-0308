package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.ConstantAPI
import com.luxpmsoft.luxaipoc.api.arcore.APIARUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import kotlinx.android.synthetic.main.activity_load_html.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.View
import android.webkit.URLUtil
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import com.luxpmsoft.luxaipoc.model.reconstruction.RenameModelRequest
import com.luxpmsoft.luxaipoc.model.reconstruction.RenameModelResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_load_html.icBack
import android.database.Cursor
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.model.ErrorModel
import kotlinx.android.synthetic.main.activity_load_html.webView
import java.io.File
import java.text.DecimalFormat

class LoadHtmlActivity : AppCompatActivity(){
    var name = ""
    var ar = ""
    var data: Rows? = null
    private var mProgressDialog: ProgressDialog? = null

    companion object {
        const val TAG = "LoadHtmlActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_html)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog?.setMessage("Please wait download file ...")
        listener()
    }

    private fun listener() {
        icBack.setOnClickListener {
            finish()
        }

        icMore.setOnClickListener {
            DialogFactory.dialogView(
                this,
                object : DialogFactory.Companion.DialogListener.ViewModelListener {
                    override fun share() {
                        checkExistFile()
                    }

                    override fun copyLink() {
                        val clipboard: ClipboardManager =
                            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("label", BuildConfig.URL_OA3D+ "reconstruction/model/"+data?.reconstructionID)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(
                            this@LoadHtmlActivity,
                            getString(R.string.link_copied_to_clipboard),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun download() {
                        if (ar.isNotEmpty()) {
                            getModelUrl(name)
                        } else {
                            downloadModel(BuildConfig.URL_OA3D+ "reconstruction/model/"+data?.reconstructionID)
                        }
                    }

                    override fun renameModel() {
                        DialogFactory.dialogRenameModel(
                            this@LoadHtmlActivity,
                            object : DialogFactory.Companion.DialogListener.RenameModel {
                                override fun renameModel(newName: String) {
                                    rename(newName)
                                }
                            }, resources.getString(R.string.rename_model))
                    }

                    override fun deleteModel() {

                    }
                })
        }

        tvViewInYourSpace.setOnClickListener {
            data?.reconstructionID?.let { it1 -> startViewSpace(it1) }
        }

        val bundle = intent.extras
        if (bundle != null) {
            data = Gson().fromJson(bundle.getString("data"), Rows::class.java)
            tvNameModel.text = data?.modelName
            tvType.text = data?.uploadDateTime?.let { MyUtils.convertToLocalTime(it)}
//            val url = bundle.getString("url")
//            name = bundle.getString("name").toString()
//            ar = bundle.getString("ar").toString()
//            Log.d(TAG, url.toString())
//            if(ar.isNotEmpty()) {
//                urlLoad = ConstantAPI.AR_BASE_URL+ ConstantAPI.VIEW_MODELS
//                webView.loadDataWithBaseURL(ConstantAPI.AR_BASE_URL+ ConstantAPI.VIEW_MODELS, url.toString(), "text/html", "UTF-8", null)
//            } else {
//                urlLoad = ConstantAPI.BASE_URL+ ConstantAPI.VIEW_MODELS
//                webView.loadDataWithBaseURL(ConstantAPI.BASE_URL+ ConstantAPI.VIEW_MODELS, url.toString(), "text/html", "UTF-8", null)
//            }
            if (bundle.getString("organization") != null) {
                tvNameModel.visibility = View.GONE
                tvType.visibility = View.GONE
                lineOrganization.visibility = View.VISIBLE
                tvTime.text = data?.uploadDateTime?.let { MyUtils.convertDateTimeHH(it)}
                data?.fileSize?.let {
                    tvSize.text = getString(R.string.size).plus(DecimalFormat("##.##").format(it.toDouble()/(1024*1024))).plus(" MB")
                }
            }
            MyUtils.loadModel(this, webView, data?.reconstructionID)
        }
    }

    fun startViewSpace(id: String) {
        val intent = Intent(this, ViewSpaceActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    fun createShare() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val screenshotUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+data?.reconstructionID+".gltf")
        shareIntent.type = "model/gltf+json"
        shareIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(shareIntent, "Share it"))
    }

    fun checkExistFile() {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+data?.reconstructionID+".gltf")
        if (file.exists()) {
            createShare()
        } else {
            mProgressDialog?.show()
            downloadModel(BuildConfig.URL_OA3D+ "reconstruction/model/"+data?.reconstructionID)
        }
    }


    fun rename(newName: String) {
        MyUtils.showProgress(this, flProgressRename)
        val rename = RenameModelRequest()
        rename.reconstructionID = data?.reconstructionID
        rename.newModelName = newName
        APIOpenAirUtils.renameModel((application as LidarApp).prefManager!!.getToken(), rename, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@LoadHtmlActivity, flProgressRename)
                val r = result as RenameModelResponse
                tvNameModel.text = r.body?.modelName
                tvType.text = r.body?.reconstructionType
                Toast.makeText(
                    this@LoadHtmlActivity,
                    getString(R.string.rename_success),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@LoadHtmlActivity, flProgressRename)
                MyUtils.toastError(this@LoadHtmlActivity, error as ErrorModel)
            }
        })
    }

    fun getModelUrl(sessionId: String?) {
        APIARUtils.getModelUrl(ConstantAPI.USER_ID, sessionId!!, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as String
                try {
                    downloadModel(data)
                } catch (e: Exception) {
                    e.message
                }
                Toast.makeText(
                    this@LoadHtmlActivity,
                    "Download Success",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(error: Any?) {
                Toast.makeText(
                    this@LoadHtmlActivity,
                    "Get Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    @SuppressLint("Range")
    fun downloadModel(dat: String) {
        val downloadManager: DownloadManager.Request = DownloadManager.Request(Uri.parse(dat))
        val title = URLUtil.guessFileName(dat, null, "model/gltf+json").replace(".bin", ".gltf")
        downloadManager.setTitle(title)
        downloadManager.setDescription("Downloading file please wait...")
        downloadManager.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
        val download:DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
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
                        mProgressDialog?.hide()
                        createShare()
                    }
                }
                cursor.close()
            }
        }.start()
    }
}