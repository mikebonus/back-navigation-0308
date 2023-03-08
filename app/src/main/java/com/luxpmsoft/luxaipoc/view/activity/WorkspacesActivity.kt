package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.WorkspacesAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.project_management.request.ProjectRequest
import com.luxpmsoft.luxaipoc.model.repositories.RepositoriesResponse
import com.luxpmsoft.luxaipoc.model.user.Organization
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspaceResponse
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspacesModel
import com.luxpmsoft.luxaipoc.model.workspaces.request.WorkspaceRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import com.luxpmsoft.luxaipoc.widget.DropdownSelect
import com.luxpmsoft.luxaipoc.widget.TextViewFonts
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.activity_workspaces.*
import kotlinx.android.synthetic.main.activity_workspaces.flProgress
import kotlinx.android.synthetic.main.activity_workspaces.icBack
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class WorkspacesActivity: BaseActivity(), WorkspacesAdapter.OnListener, DropdownSelect.OnListener {
    var workspacesAdapter: WorkspacesAdapter? = null
    var workspacesModel: ArrayList<WorkspacesModel>? = ArrayList()
    var repositoryId = ""
    var organizationId = ""
    var isFirst = false
    var doublePressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        setContentView(R.layout.activity_workspaces)
        init()
        listener()
    }

    fun init() {
        (application as LidarApp).prefManager?.getOrganizationRole()?.let {
            if (it != "admin") {
                lnAddWorkspace.visibility = View.GONE
            }

        }
        Utils.gridLayoutManager(this, grvWorkspaces, 1, GridLayoutManager.VERTICAL)
        workspacesAdapter = WorkspacesAdapter(this, R.layout.item_workspaces, workspacesModel!!, this)
        grvWorkspaces.adapter = workspacesAdapter
        val bundle = intent.extras
        if (bundle != null) {
            repositoryId = bundle?.getString("repositoryId").toString()
//            tvRepositoryName.text = bundle?.getString("repositoryName").toString()
            organizationId = bundle?.getString("organizationId").toString()
            getRepositories()
        }
    }

    override fun onResume() {
        super.onResume()
        getWorkspace(repositoryId)
    }

    fun listener() {
        spnRepository.setOnListener(this)
        icBack.setOnClickListener {
            finish()
        }

        lnAddWorkspace.setOnClickListener {
            if (doublePressedOnce) {
                return@setOnClickListener
            }

            doublePressedOnce = true

            Handler().postDelayed(Runnable { doublePressedOnce = false }, 1100)
            DialogFactory.dialogCreateWorkspace(
                this,
                object : DialogFactory.Companion.DialogListener.CreateWorkspace {
                    override fun createWorkspace(name: String, description: String) {
                        createWS(name, description)
                    }
                })
        }
    }

    override fun onListener(workspaceId: String) {
        val intent = Intent(this, ProjectActivity::class.java)
        intent.putExtra("repositoryId", repositoryId)
        intent.putExtra("workspaceId", workspaceId)
        startActivity(intent)
        MyUtils.transitionAnimation(this)
    }

    override fun onCreateProject(
        workspaceId: String,
        workspaceName: String,
        view: View?,
        position: Int
    ) {
        showPopupCreateProject(view, workspaceId, workspaceName, position)
    }

    private fun showPopupCreateProject(anchor: View?, workspaceId: String, workspaceName: String, position: Int) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.popup_create_project, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }
            contentView.findViewById<TextViewFonts>(R.id.tvEditWorkspace)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(this@WorkspacesActivity, EditWorkspaceActivity::class.java)
                    intent.putExtra("workspace", Gson().toJson(workspacesModel?.get(position)))
                    startActivity(intent)
                    dismiss()
                })
            contentView.findViewById<TextViewFonts>(R.id.tvDeleteWorkspace)
                .setOnClickListener(View.OnClickListener {
                    DialogFactory.dialogDelete(
                        this@WorkspacesActivity,
                        object : DialogFactory.Companion.DialogListener.Delete {
                            override fun delete() {
                                deleteWorkspace(organizationId, workspaceId, position)
                            }
                        }, " workplace ".plus(workspaceName))
                    dismiss()
                })
        }.also { popupWindow ->
            popupWindow.height = LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            popupWindow.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT)
            )

            // Absolute location of the anchor view
            val location = IntArray(2).apply {
                anchor?.getLocationOnScreen(this)
            }
            val size = Size(
                popupWindow.contentView.measuredWidth,
                popupWindow.contentView.measuredHeight
            )
            popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.END,
                resources.getDimensionPixelOffset(R.dimen.size_26),
                location[1] - size.height+resources.getDimensionPixelOffset(R.dimen.size_26)+popupWindow.contentView.measuredHeight
            )
        }
    }

    fun createProjectManagement(projectName: String?, workspaceId: String) {
        MyUtils.showProgress(this, flProgress)
        val projectRequest = ProjectRequest()
        projectRequest.workspaceFolderName = projectName
        APIOpenAirUtils.createProject((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), workspaceId, projectRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    Toast.makeText(
                        this@WorkspacesActivity,
                        "Create project successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    getWorkspace(repositoryId)
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    MyUtils.toastError(this@WorkspacesActivity, error as ErrorModel)
                }
            })
    }

    fun getWorkspace(repositoryId: String?) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getWorkspace(repositoryId,
            (application as LidarApp).prefManager?.getToken(), object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    val data = result as WorkspaceResponse
                    data.response?.workspaces?.let {
                        workspacesModel?.clear()
                        workspacesModel?.addAll(it)
                        Collections.sort(workspacesModel,
                            Comparator<WorkspacesModel> { o1, o2 ->
                                MyUtils.convertDatetimeToDate(o2.created_at!!)
                                .compareTo(MyUtils.convertDatetimeToDate(o1.created_at!!)) })
                    }

                    workspacesAdapter?.notifyDataSetChanged()
                    if (!isFirst) {
                        isFirst = true
                    }

                    if (workspacesModel?.isNotEmpty()!!) {
                        grvWorkspaces.visibility = View.VISIBLE
                        tvEmptyWorkspace.visibility = View.GONE
                    } else {
                        grvWorkspaces.visibility = View.GONE
                        tvEmptyWorkspace.visibility = View.VISIBLE
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    MyUtils.toastError(this@WorkspacesActivity, error as ErrorModel)
                }
            })
    }

    //delete workspace
    fun deleteWorkspace(organizationId: String, workspaceId: String, position: Int) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.deleteWorkspace((application as LidarApp).prefManager!!.getToken(),
            organizationId, workspaceId, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    Toast.makeText(
                        this@WorkspacesActivity,
                        "Delete successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    workspacesModel?.removeAt(position)
                    workspacesAdapter?.notifyDataSetChanged()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    MyUtils.toastError(this@WorkspacesActivity, error as ErrorModel)
                }
            })
    }

    fun createWS(workspaceName: String?, description: String?) {
        MyUtils.showProgress(this, flProgress)
        val wsRequest = WorkspaceRequest()
        wsRequest.workspaceName = workspaceName
        wsRequest.description = description
        wsRequest.organizationId = (application as LidarApp).prefManager!!.getOrganizationId()
        wsRequest.repositoryId = repositoryId
        APIOpenAirUtils.createWorkspace((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), wsRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    Toast.makeText(
                        this@WorkspacesActivity,
                        "Create workspace successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    workspacesModel?.clear()
                    getWorkspace(repositoryId)
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    MyUtils.toastError(this@WorkspacesActivity, error as ErrorModel)
                }
            })
    }

    fun getRepositories() {
        APIOpenAirUtils.getRepositories((application as LidarApp).prefManager!!.getOrganizationId(),
            (application as LidarApp).prefManager!!.getToken(), object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    val data = result as RepositoriesResponse
                    data.orgRepositoryInfo?.let {
                        val dropdownList: ArrayList<Organization> = ArrayList()
                        for (repo in it) {
                            val data = Organization()
                            data.organizationId = repo.repositoryId
                            data.name = repo.repositoryName
                            dropdownList.add(data)
                        }
                        spnRepository.setData(dropdownList)
                        spnRepository.setSelection(repositoryId)
                    }

                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@WorkspacesActivity, flProgress)
                    MyUtils.toastError(this@WorkspacesActivity, error as ErrorModel)
                }
            })
    }

    override fun onChoose(view: View, id: String?) {
        if (isFirst) {
            repositoryId = id.toString()
            getWorkspace(repositoryId)
        }
    }
}