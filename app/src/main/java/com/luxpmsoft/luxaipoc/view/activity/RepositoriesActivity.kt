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
import com.luxpmsoft.luxaipoc.adapter.RepositoriesAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.repositories.RepositoriesModel
import com.luxpmsoft.luxaipoc.model.repositories.RepositoriesResponse
import com.luxpmsoft.luxaipoc.model.repositories.request.RepositoryRequest
import com.luxpmsoft.luxaipoc.model.user.MeResponse
import com.luxpmsoft.luxaipoc.model.user.Organization
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import com.luxpmsoft.luxaipoc.widget.DropdownSelect
import com.luxpmsoft.luxaipoc.widget.TextViewFonts
import kotlinx.android.synthetic.main.activity_repositories.*

class RepositoriesActivity: BaseActivity(), RepositoriesAdapter.OnListener, DropdownSelect.OnListener {
    var repositoriesAdapter: RepositoriesAdapter? = null
    var repositoriesModel: ArrayList<RepositoriesModel>? = ArrayList()
    var isFirst = false
    var doublePressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        setContentView(R.layout.activity_repositories)

        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            bundle?.getString("organizationName")?.let {
                //hide
//                tvSiteName.text = bundle?.getString("organizationName").toString()
            }
        }

        (application as LidarApp).prefManager?.getOrganizationRole()?.let {
            if (it != "admin") {
                lnAddRepository.visibility = View.GONE
            }
        }

        Utils.gridLayoutManager(this, grvRepositories, 1, GridLayoutManager.VERTICAL)
        repositoriesAdapter = RepositoriesAdapter(this, R.layout.item_repositories, repositoriesModel!!, this)
        grvRepositories.adapter = repositoriesAdapter
        getMe()

    }

    override fun onResume() {
        super.onResume()
        getRepositories()

    }

    fun listener() {
        spnSites.setOnListener(this)
        icBack.setOnClickListener {
            finish()
        }

        lnAddRepository.setOnClickListener {
            if (doublePressedOnce) {
                return@setOnClickListener
            }

            doublePressedOnce = true

            Handler().postDelayed(Runnable { doublePressedOnce = false }, 1100)
            DialogFactory.dialogCreateRepo(
                this,
                object : DialogFactory.Companion.DialogListener.CreateRepo {
                    override fun createRepo(name: String, isPublic: String) {
                        createRepository(name, isPublic)
                    }
                })
        }
    }

    override fun onListener(repositoryId: String, repositoryName: String, organizationId: String) {
        val intent = Intent(this, WorkspacesActivity::class.java)
        intent.putExtra("repositoryId", repositoryId)
        intent.putExtra("organizationId", organizationId)
        intent.putExtra("repositoryName", repositoryName)
        startActivity(intent)
        MyUtils.transitionAnimation(this)
    }

    override fun onMore(
        repositoryId: String,
        repositoryName: String,
        view: View?,
        position: Int
    ) {
        showPopupMore(view, repositoryId, repositoryName,  position)
    }

    private fun showPopupMore(
        anchor: View?,
        repositoryId: String,
        repositoryName: String,
        position: Int
    ) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.popup_repository, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }

            if ((application as LidarApp).prefManager?.getOrganizationRole() == "admin" &&
                    Gson().toJson(repositoriesModel?.get(position)?.isPublic).equals("false")) {
                contentView.findViewById<TextViewFonts>(R.id.tvManageUsers).visibility = View.VISIBLE
            }

            contentView.findViewById<TextViewFonts>(R.id.tvManageUsers)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(this@RepositoriesActivity, ManageUserActivity::class.java)
                    intent.putExtra("repository", Gson().toJson(repositoriesModel?.get(position)))
                    startActivity(intent)
                    dismiss()
                })

            contentView.findViewById<TextViewFonts>(R.id.tvEditRepository)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(this@RepositoriesActivity, EditRepositoryActivity::class.java)
                    intent.putExtra("repository", Gson().toJson(repositoriesModel?.get(position)))
                    startActivity(intent)
                    dismiss()
                })
            contentView.findViewById<TextViewFonts>(R.id.tvDeleteRepository)
                .setOnClickListener(View.OnClickListener {
                    DialogFactory.dialogDelete(
                        this@RepositoriesActivity,
                        object : DialogFactory.Companion.DialogListener.Delete {
                            override fun delete() {
                                deleteRepository(repositoryId, position)
                            }
                        }, " repository ".plus(repositoryName))
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

    fun getRepositories() {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getRepositories((application as LidarApp).prefManager!!.getOrganizationId(),
            (application as LidarApp).prefManager!!.getToken(), object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@RepositoriesActivity, flProgress)
                    val data = result as RepositoriesResponse
                    data.orgRepositoryInfo?.let {
                        repositoriesModel?.clear()
                        repositoriesModel?.addAll(it)
                    }

                    repositoriesAdapter?.notifyDataSetChanged()
                    if (!isFirst) {
                        isFirst = true
                    }

                    if (repositoriesModel?.isNotEmpty()!!) {
                        grvRepositories.visibility = View.VISIBLE
                        tvEmptyRepository.visibility = View.GONE
                    } else {
                        grvRepositories.visibility = View.GONE
                        tvEmptyRepository.visibility = View.VISIBLE
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@RepositoriesActivity, flProgress)
                    MyUtils.toastError(this@RepositoriesActivity, error as ErrorModel)
                }
            })
    }

    override fun onChoose(view: View, id: String?) {
        if (isFirst) {
            (application as LidarApp).prefManager?.setOrganizationId(
                id
            )
            getRepositories()
        }
    }

    fun createRepository(repoName: String?, isPublic: String?) {
        MyUtils.showProgress(this, flProgress)
        val repoRequest = RepositoryRequest()
        repoRequest.repositoryName = repoName
        repoRequest.organizationId = (application as LidarApp).prefManager!!.getOrganizationId()
        repoRequest.repositoryPhoto = ""
        repoRequest.isPublic = isPublic

        APIOpenAirUtils.createRepository((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), repoRequest, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@RepositoriesActivity, flProgress)
                Toast.makeText(
                    this@RepositoriesActivity,
                    "Create repository successfully",
                    Toast.LENGTH_SHORT
                ).show()
                repositoriesModel?.clear()
                getRepositories()
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@RepositoriesActivity, flProgress)
                MyUtils.toastError(this@RepositoriesActivity, error as ErrorModel)
            }
        })
    }

    fun getMe() {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getMe((application as LidarApp).prefManager!!.getToken() , object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val me = result as MeResponse
                me.body?.organizations?.let {
                    val dropdownList: ArrayList<Organization> = ArrayList()
                    dropdownList.addAll(it)
                    spnSites.setData(dropdownList)
                    spnSites.setSelection((application as LidarApp).prefManager!!.getOrganizationId())
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@RepositoriesActivity, flProgress)
                MyUtils.toastError(this@RepositoriesActivity, error as ErrorModel)
            }
        })
    }

    fun deleteRepository(repositoryId: String?, position: Int) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.removeRepository((application as LidarApp).prefManager!!.getToken(),
            repositoryId, (application as LidarApp).prefManager!!.getOrganizationId(), object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@RepositoriesActivity, flProgress)
                    Toast.makeText(
                        this@RepositoriesActivity,
                        "Delete successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    repositoriesModel?.removeAt(position)
                    repositoriesAdapter?.notifyDataSetChanged()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@RepositoriesActivity, flProgress)
                    MyUtils.toastError(this@RepositoriesActivity, error as ErrorModel)
                }
            })
    }
}