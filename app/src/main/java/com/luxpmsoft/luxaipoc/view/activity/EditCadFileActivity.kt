package com.luxpmsoft.luxaipoc.view.activity

import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.recentmodel.CadFileRows
import com.luxpmsoft.luxaipoc.model.recentmodel.request.SaveCadFileRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_edit_cad_file.*

class EditCadFileActivity: BaseActivity() {
    var cadFile: CadFileRows? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent))
        window.setGravity(Gravity.CENTER_HORIZONTAL)
        setContentView(R.layout.activity_edit_cad_file)
        setFinishOnTouchOutside(true)
        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        bundle?.let {
            cadFile = Gson().fromJson(it.getString("cadfile").toString(), CadFileRows::class.java)
            cadFile?.let {
                edtCadFileName.setText(it.name)
                it.description?.let {
                    edtDescription.setText(it)
                }
            }
        }

    }

    fun listener() {
        btEditCadFile.setOnClickListener {
            saveCadFile()
        }

        btCancel.setOnClickListener {
            finish()
        }
    }

    fun saveCadFile() {
        MyUtils.showProgress(this, flProgress)
        var request = SaveCadFileRequest()
        request.name = edtCadFileName.text.trim().toString()
        request.description = edtDescription.text.trim().toString()
        APIOpenAirUtils.saveCadFile((application as LidarApp).prefManager!!.getToken(),
            cadFile?.cadFileID, request, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@EditCadFileActivity, flProgress)
                    Toast.makeText(
                        this@EditCadFileActivity,
                        getString(R.string.update_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@EditCadFileActivity, flProgress)
                    MyUtils.toastError(this@EditCadFileActivity, error as ErrorModel)
                    cadFile?.let {
                        edtCadFileName.setText(it.name)
                        edtDescription.setText(it.description)
                    }
                }
            })
    }
}