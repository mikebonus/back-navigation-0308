package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
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
import com.luxpmsoft.luxaipoc.model.repositories.OrganizationUser
import com.luxpmsoft.luxaipoc.model.repositories.RepositoriesModel
import com.luxpmsoft.luxaipoc.model.repositories.RepositoryUsers
import com.luxpmsoft.luxaipoc.model.repositories.UsersRepositoryResponse
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspacesModel
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import kotlinx.android.synthetic.main.activity_edit_repository.*
import kotlinx.android.synthetic.main.activity_manage_user.*
import kotlinx.android.synthetic.main.activity_manage_user.flProgress
import java.util.*
import kotlin.collections.ArrayList

class ManageUserActivity : BaseActivity(), UsersAdapter.OnListener {

    var userAdapter: UsersAdapter? = null
    var usersModel: ArrayList<OrganizationUser>? = ArrayList()
    var repo: RepositoriesModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_user)

        init()
        listener()
    }

    fun init() {
        Utils.gridLayoutManager(this, grvUsers, 1, GridLayoutManager.VERTICAL)
        userAdapter = UsersAdapter(this, R.layout.item_users, usersModel!!, this, true)
        grvUsers.adapter = userAdapter

        val bundle = intent.extras
        bundle?.let {
            repo = Gson().fromJson(it.getString("repository").toString(), RepositoriesModel::class.java)
        }
    }

    fun listener() {

        icBack.setOnClickListener {
            finish()
        }

        lnAddUser.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            intent.putExtra("repository", Gson().toJson(repo))
            startActivity(intent)
        }
    }

    fun usersRepository(repositoryId: String?) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.userRepository((application as LidarApp).prefManager!!.getToken(),
            repositoryId, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@ManageUserActivity, flProgress)
                    val data = result as UsersRepositoryResponse
                    data.message?.let {
                        usersModel?.clear()
                        for (user in it) {
                            user.pos = 0
                            if (user.user?.uid == (application as LidarApp).prefManager!!.getUserId()) {
                                user.pos = 1
                            }
                            usersModel?.add(user)
                        }
                        Collections.sort(usersModel,
                            Comparator<OrganizationUser> { o1, o2 ->
                               o2.pos!!.compareTo(o1.pos!!) })
                        repo?.users = usersModel
                        userAdapter?.notifyDataSetChanged()
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@ManageUserActivity, flProgress)
                    MyUtils.toastError(this@ManageUserActivity, error as ErrorModel)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        usersRepository(repo?.repositoryId)
    }

    override fun onListener(repositoryId: String, repositoryName: String) {
        TODO("Not yet implemented")
    }

    override fun onDelete(userId: String, userName: String, position: Int) {
        DialogFactory.dialogDelete(
            this@ManageUserActivity,
            object : DialogFactory.Companion.DialogListener.Delete {
                override fun delete() {
                    deleteUser(userId, position)
                }
            }, " user ".plus(userName))
    }

    override fun onSelect(position: Int, isCheck: Boolean) {
        TODO("Not yet implemented")
    }

    fun deleteUser(repositoryUserId: String?, position: Int) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.deleteUser((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getOrganizationId(), repositoryUserId!!, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@ManageUserActivity, flProgress)
                    Toast.makeText(
                        this@ManageUserActivity,
                        "Delete successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    usersModel?.removeAt(position)
                    userAdapter?.notifyDataSetChanged()
                }

                override fun onError(error: Any?) {
                    Toast.makeText(
                        this@ManageUserActivity,
                        "Not deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    MyUtils.hideProgress(this@ManageUserActivity, flProgress)
                    MyUtils.toastError(this@ManageUserActivity, error as ErrorModel)
                }
            })
    }
}