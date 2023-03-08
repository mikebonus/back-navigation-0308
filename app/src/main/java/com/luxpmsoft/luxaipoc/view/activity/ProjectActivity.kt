package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.ExpandableListAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.project_management.ProjectManagementResponse
import com.luxpmsoft.luxaipoc.model.project_management.request.BoardRequest
import com.luxpmsoft.luxaipoc.model.project_management.request.ProjectRequest
import com.luxpmsoft.luxaipoc.model.recentmodel.CadFileResponse
import com.luxpmsoft.luxaipoc.model.user.Organization
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspaceFolder
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspaceFolderBoard
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspaceResponse
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspacesModel
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import com.luxpmsoft.luxaipoc.widget.DropdownSelect
import kotlinx.android.synthetic.main.activity_file.*
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.activity_project.flProgress
import kotlinx.android.synthetic.main.activity_project.icBack
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProjectActivity: BaseActivity(), ExpandableListAdapter.OnListener, DropdownSelect.OnListener {
    private var adapter: ExpandableListAdapter? = null
    // Array list for header
    val header = ArrayList<WorkspaceFolder>()
    // Hash map for both header and child
    val hashMap = HashMap<WorkspaceFolder, List<WorkspaceFolderBoard>>()
    var repositoryId = ""
    var workspaceId = ""
    var organizationId = ""
    var isFirst = false
    var doublePressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        setListener()
        listener()
    }

    fun init() {
        expandable_listview.setGroupIndicator(null)


        val bundle = intent.extras
        if (bundle != null) {
            workspaceId = bundle?.getString("workspaceId").toString()
            val bundle = intent.extras
            if (bundle != null) {
                repositoryId = bundle?.getString("repositoryId").toString()
                getWorkspace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getProjectManagement(workspaceId)
    }

    fun listener() {
        spnProject.setOnListener(this)
        icBack.setOnClickListener {
            finish()
        }

        lnAddProject.setOnClickListener {
            if (doublePressedOnce) {
                return@setOnClickListener
            }

            doublePressedOnce = true

            Handler().postDelayed(Runnable { doublePressedOnce = false }, 1100)

            DialogFactory.dialogCreateProject(
                this,
                object : DialogFactory.Companion.DialogListener.CreateProject {
                    override fun createProject(name: String) {
                        createProjectManagement(name)
                    }
                })
        }
    }

    // Setting different listeners to expandablelistview
    fun setListener() {

        // This listener will show toast on group click
        expandable_listview.setOnGroupClickListener(ExpandableListView.OnGroupClickListener { listview, view, group_pos, id ->
            false
        })

        // This listener will expand one group at one time
        // You can remove this listener for expanding all groups
        expandable_listview
            .setOnGroupExpandListener(object : ExpandableListView.OnGroupExpandListener {
                // Default position
                var previousGroup = -1
                override fun onGroupExpand(groupPosition: Int) {
                    expandable_listview.collapseGroup(previousGroup)
                }
            })



        // This listener will show toast on child click
        expandable_listview.setOnChildClickListener(ExpandableListView.OnChildClickListener { listview, view, groupPos, childPos, id ->
            (adapter?.getChild(groupPos, childPos) as WorkspaceFolderBoard).workspaceFolderBoardId?.let {
                val intent = Intent(this, BoardFileManagerActivity::class.java)
                intent.putExtra("boardId", (adapter?.getChild(groupPos, childPos) as WorkspaceFolderBoard).workspaceFolderBoardId)
                intent.putExtra("boardName", (adapter?.getChild(groupPos, childPos) as WorkspaceFolderBoard).workspaceFolderBoardName)
                intent.putExtra("projectName",(adapter?.getGroup(groupPos) as WorkspaceFolder).workspaceFolderName.toString())
                startActivity(intent)
                MyUtils.transitionAnimation(this)
            }
            false
        })
    }

    fun getWorkspace() {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getWorkspace(repositoryId,
            (application as LidarApp).prefManager?.getToken(), object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    val data = result as WorkspaceResponse
                    data.response?.workspaces?.let {
                        val dropdownList: ArrayList<Organization> = ArrayList()
                        for (workspace in it) {
                            val data = Organization()
                            data.organizationId = workspace.workspaceId
                            data.name = workspace.workspaceName
                            dropdownList.add(data)
                        }
                        spnProject.setData(dropdownList)
                        spnProject.setSelection(workspaceId)
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    MyUtils.toastError(this@ProjectActivity, error as ErrorModel)
                }
            })
    }

    fun getProjectManagement(workspaceId: String?) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getProjectManagement(workspaceId,
            (application as LidarApp).prefManager?.getToken(), object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    val data = result as ProjectManagementResponse
                    data.response?.folders?.let {
                        header?.clear()
                        hashMap.clear()
                        it.forEachIndexed { index, folder ->
                            folder?.workspaceFolderName?.let { it1 -> header.add(folder) }
                            val child: MutableList<WorkspaceFolderBoard> = ArrayList()
                            folder?.boards?.let {
                                it.forEachIndexed { index, folder ->
                                    folder.workspaceFolderBoardName?.let {
                                            it1 -> child.add(folder)
                                        getTotal(folder.workspaceFolderBoardId, index, child)
                                    }
                                }
                            }
                            if (child.isEmpty()) {
                                child.add(WorkspaceFolderBoard())
                            }
                            hashMap[header[index]] = child
                        }

                        adapter = ExpandableListAdapter(this@ProjectActivity, header, hashMap, this@ProjectActivity)
                        // Setting adpater over expandablelistview
                        expandable_listview.setAdapter(adapter)
                        header.forEachIndexed { i, e ->
                            expandable_listview.expandGroup(i)
                        }
                    }
                    if (!isFirst) {
                        isFirst = true
                    }

                    if (header.isNotEmpty()) {
                        expandable_listview.visibility = View.VISIBLE
                        lineEmpty.visibility = View.GONE
                    } else {
                        expandable_listview.visibility = View.GONE
                        lineEmpty.visibility = View.VISIBLE
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    MyUtils.toastError(this@ProjectActivity, error as ErrorModel)
                }
            })
    }

    fun createProjectManagement(projectName: String?) {
        MyUtils.showProgress(this, flProgress)
        val projectRequest = ProjectRequest()
        projectRequest.workspaceFolderName = projectName
        APIOpenAirUtils.createProject((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), workspaceId, projectRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    Toast.makeText(
                        this@ProjectActivity,
                        "Create project successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    getProjectManagement(workspaceId)
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    MyUtils.toastError(this@ProjectActivity, error as ErrorModel)
                }
            })
    }

    fun createBoardToWorkspace(boardName: String?, boardType: String?, workspaceFolderId: String?) {
        MyUtils.showProgress(this, flProgress)
        val boardRequest = BoardRequest()
        boardRequest.boardName = boardName
        boardRequest.boardType = boardType
        APIOpenAirUtils.createBoard((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), workspaceFolderId, boardRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    Toast.makeText(
                        this@ProjectActivity,
                        getString(R.string.create_board_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    header?.clear()
                    hashMap.clear()
                    getProjectManagement(workspaceId)
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    MyUtils.toastError(this@ProjectActivity, error as ErrorModel)
                }
            })
    }

    fun deleteProject(folderId: String?) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.deleteProject((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), folderId, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    Toast.makeText(
                        this@ProjectActivity,
                        "Delete successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    header?.clear()
                    hashMap.clear()
                    getProjectManagement(workspaceId)
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    MyUtils.toastError(this@ProjectActivity, error as ErrorModel)
                }
            })
    }

    override fun onOptions(workspaceFolder: WorkspaceFolder?, view: View?) {
//        showPopupWindow(view, workspaceFolder)
        val wrapper = ContextThemeWrapper(this, R.style.PopupMenu)
        val popup = PopupMenu(wrapper, view!!)
        popup.setGravity(Gravity.END);
        popup.menuInflater.inflate(R.menu.menu, popup.menu)
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                val i: Int = item.getItemId()
                return if (i == R.id.lineAddNewBoard) {
                    DialogFactory.dialogCreateBoard(
                        this@ProjectActivity,
                        object : DialogFactory.Companion.DialogListener.CreateBoard {
                            override fun createBoard(name: String, type:String) {
                                createBoardToWorkspace(name, "Cadfile", workspaceFolder?.workspaceFolderId)
                            }
                        })
                    true
                } else {
                    DialogFactory.dialogDelete(
                        this@ProjectActivity,
                        object : DialogFactory.Companion.DialogListener.Delete {
                            override fun delete() {
                                deleteProject(workspaceFolder?.workspaceFolderId)
                            }
                        }, " project ".plus(workspaceFolder?.workspaceFolderName))
                    true
                }
            }
        })

        popup.show()
    }

    override fun odDeleteBoard(boardId: String?, boardName: String?) {
        DialogFactory.dialogDelete(
            this,
            object : DialogFactory.Companion.DialogListener.Delete {
                override fun delete() {
                    removeBoard(boardId)
                }
            }, " board ".plus(boardName))
    }

    fun removeBoard(boardId: String?) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.deleteBoard((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), boardId, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    Toast.makeText(
                        this@ProjectActivity,
                        getString(R.string.delete_board_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    getProjectManagement(workspaceId)
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@ProjectActivity, flProgress)
                    MyUtils.toastError(this@ProjectActivity, error as ErrorModel)
                }
            })
    }

    fun getTotal(boardId: String?, index: Int?, child: MutableList<WorkspaceFolderBoard>) {
        APIOpenAirUtils.getCadFile((application as LidarApp).prefManager!!.getToken(), null, null,
            boardId, null, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    val data = result as CadFileResponse
                    data.response?.let {
                        child[index!!].total = it.count
                        adapter?.notifyDataSetInvalidated()
                    }
                }

                override fun onError(error: Any?) {
                }
            })
    }

    private fun showPopupWindow(anchor: View?, workspaceFolder: WorkspaceFolder?) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.dialog_project_options, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }
            contentView.findViewById<LinearLayoutCompat>(R.id.lineAddNewBoard)
                .setOnClickListener(View.OnClickListener {
                    DialogFactory.dialogCreateBoard(
                        this@ProjectActivity,
                        object : DialogFactory.Companion.DialogListener.CreateBoard {
                            override fun createBoard(name: String, type:String) {
                                createBoardToWorkspace(name, "Cadfile", workspaceFolder?.workspaceFolderId)
                            }
                        })

                    dismiss()
                })

            contentView.findViewById<LinearLayoutCompat>(R.id.lineDeleteProject)
                .setOnClickListener(View.OnClickListener {
                    DialogFactory.dialogDelete(
                        this@ProjectActivity,
                        object : DialogFactory.Companion.DialogListener.Delete {
                            override fun delete() {
                                deleteProject(workspaceFolder?.workspaceFolderId)
                            }
                        }, " project ".plus(workspaceFolder?.workspaceFolderName))
                    dismiss()
                })
        }.also { popupWindow ->
            popupWindow.width = resources.getDimension(R.dimen.size_180).toInt()
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
                Gravity.END,
                0,
                -size.height-40
            )
        }
    }

    override fun onChoose(view: View, id: String?) {
        if (isFirst) {
            workspaceId = id.toString()
            getProjectManagement(workspaceId)
        }
    }
}