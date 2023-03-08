package com.luxpmsoft.luxaipoc.view.activity

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.UsersAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.organization.OrganizationUsersResponse
import com.luxpmsoft.luxaipoc.model.repositories.OrganizationUser
import com.luxpmsoft.luxaipoc.model.repositories.RepositoriesModel
import com.luxpmsoft.luxaipoc.model.repositories.request.AddUserRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_add_user.*

class AddUserActivity: BaseActivity(), UsersAdapter.OnListener {
    var userAdapter: UsersAdapter? = null
    var usersModel: ArrayList<OrganizationUser>? = ArrayList()
    var users: ArrayList<OrganizationUser>? = ArrayList()
    var check = false
    var repo: RepositoriesModel? = null
    private val customHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        setContentView(R.layout.activity_add_user)

        init()
        listener()
    }

    fun init() {
        Utils.gridLayoutManager(this, grvAddUser, 1, GridLayoutManager.VERTICAL)
        userAdapter = UsersAdapter(this, R.layout.item_users, usersModel!!, this, false)
        grvAddUser.adapter = userAdapter
        val bundle = intent.extras
        bundle?.let {
            repo = Gson().fromJson(it.getString("repository").toString(), RepositoriesModel::class.java)
            repo?.let {
                getOrganizationDetail()
            }
        }
    }

    fun listener() {
        icBack.setOnClickListener {
            finish()
        }

        tvSave.setOnClickListener {
            usersModel?.forEachIndexed { index, element ->
                if (element.isCheck == true) {
                    element.organizationUserId?.let { it1 -> addUser(it1) }
                }
            }
        }

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                customHandler.removeCallbacks(getFile)
                customHandler.postDelayed(getFile, 600)
                s?.let {
                    if (it.isNotEmpty()) {
                        icClear.visibility = View.VISIBLE
                    } else {
                        icClear.visibility = View.INVISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        icClear.setOnClickListener {
            edtSearch.setText("")
            icClear.visibility = View.INVISIBLE
        }
    }

    private val getFile: Runnable = object : Runnable {
        override fun run() {
            usersModel?.clear()
            if (edtSearch.text.toString().isNotEmpty()) {
                for (user in users!!) {
                    if (user.user?.full_name!!.lowercase().contains(edtSearch.text.toString().lowercase())) {
                        usersModel?.add(user)
                    }
                }
            } else {
                usersModel?.addAll(users!!)
            }

            userAdapter?.notifyDataSetChanged()
        }
    }

    fun getOrganizationDetail() {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getOrganizationDetail((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@AddUserActivity, flProgress)
                    val data = result as OrganizationUsersResponse
                    data.organizationUsers?.rows?.let {
                        for (userTotal in it) {
                            val user: OrganizationUser? = userTotal
                            for (userExist in repo?.users!!) {
                                if(userTotal.user?.uid!! == userExist.user?.uid!!) {
                                    check = true
                                }
                            }
                            if (check == false) {
                                user?.let { it1 -> users?.add(it1) }
                            }
                            check = false
                        }
                    }
                    users?.let {
                        usersModel?.addAll(it)
                        userAdapter?.notifyDataSetChanged()
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@AddUserActivity, flProgress)
                    MyUtils.toastError(this@AddUserActivity, error as ErrorModel)
                }
            })
    }

    fun addUser(userId: String) {
        MyUtils.showProgress(this, flProgress)
        val request = AddUserRequest()
        request.organizationId = (application as LidarApp).prefManager!!.getOrganizationId()
        request.usertoadd = userId
            APIOpenAirUtils.addUser((application as LidarApp).prefManager!!.getToken(),
                repo?.repositoryId, request, object : APIInterface.onDelegate {
                    override fun onSuccess(result: Any?) {
                        MyUtils.hideProgress(this@AddUserActivity, flProgress)
                        Toast.makeText(
                            this@AddUserActivity,
                            "Add user successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }

                    override fun onError(error: Any?) {
                        MyUtils.hideProgress(this@AddUserActivity, flProgress)
                        MyUtils.toastError(this@AddUserActivity, error as ErrorModel)
                    }
                })
    }

    override fun onListener(repositoryId: String, repositoryName: String) {

    }

    override fun onDelete(userId: String, userName: String, position: Int) {

    }

    override fun onSelect(position: Int, isCheck: Boolean) {
        usersModel?.get(position)?.isCheck = isCheck
        var isSave = false
        usersModel?.forEachIndexed { index, element ->
            if (element.isCheck == true) {
                isSave = true
                return@forEachIndexed
            }
        }
        if (isSave) {
            tvSave.visibility = View.VISIBLE
        } else {
            tvSave.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        usersModel = null
        users = null
    }
}