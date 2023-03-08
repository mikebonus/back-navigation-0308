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
import com.luxpmsoft.luxaipoc.model.workspaces.EditWorkspaceResponse
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspacesModel
import com.luxpmsoft.luxaipoc.model.workspaces.request.EditWorkspaceRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_edit_workspace.*

class EditWorkspaceActivity: BaseActivity() {
    var workspace: WorkspacesModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent))
        window.setGravity(Gravity.CENTER_HORIZONTAL)
        setContentView(R.layout.activity_edit_workspace)

        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        bundle?.let {
            workspace = Gson().fromJson(it.getString("workspace").toString(), WorkspacesModel::class.java)
            workspace?.let {
                edtWorkspaceName.setText(it.workspaceName)
                edtDescription.setText(it.description)
            }
        }
    }

    fun listener() {

        btCancel.setOnClickListener {
            finish()
        }

        btCreate.setOnClickListener {
            editWorkspace()
        }
    }

    fun editWorkspace() {
        MyUtils.showProgress(this, flProgress)
        var request = EditWorkspaceRequest()
        request.workspaceName = edtWorkspaceName.text.toString()
        request.description = edtDescription.text.toString()
        APIOpenAirUtils.editWorkspace((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), workspace?.workspaceId, request, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@EditWorkspaceActivity, flProgress)
                    val data = result as EditWorkspaceResponse
                    Toast.makeText(
                        this@EditWorkspaceActivity,
                        getString(R.string.update_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@EditWorkspaceActivity, flProgress)
                    MyUtils.toastError(this@EditWorkspaceActivity, error as ErrorModel)
                    workspace?.let {
                        edtWorkspaceName.setText(it.workspaceName)
                        edtDescription.setText(it.description)
                    }
                }
            })
    }
}