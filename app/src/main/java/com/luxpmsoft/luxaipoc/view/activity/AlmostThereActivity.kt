package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.home.SubscriptionResponse
import com.luxpmsoft.luxaipoc.model.organization.OrganizationCheckResponse
import com.luxpmsoft.luxaipoc.model.organization.OrganizationJoinExistRequest
import com.luxpmsoft.luxaipoc.model.organization.OrganizationRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeDetailResponse
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.login.SignInActivity
import com.luxpmsoft.luxaipoc.widget.BottomCreateOrganization
import com.luxpmsoft.luxaipoc.widget.IAdapterClickListener
import kotlinx.android.synthetic.main.activity_almost_there.*
class AlmostThereActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_almost_there)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        bundle?.let {
            lineAlmostThere.visibility = View.GONE
            lineApprovedPending.visibility = View.VISIBLE
        }
    }

    fun listener() {
        tvIndividual.setOnClickListener {
            getSubType("Individual", null)
        }

        tvApprovedPending.setOnClickListener {
            finish()
        }

        tvOrganization.setOnClickListener {
            val menu = BottomCreateOrganization(
                object : IAdapterClickListener {
                    override fun onClickListener(id: Int?, obj: Any?) {
                        val orgName = obj as String
                        if (id == 0) {
                            getSubType("Organization", orgName)
//                            createOrganization(orgName)
                        } else {
                            organizationJoinExisting(orgName)
                        }
                    }
                })

            menu.show(supportFragmentManager, menu.tag)
        }
    }

    fun startHome(type: String) {
        val intent: Intent
        if (type.lowercase() == "individual") {
            intent = Intent(this, HomeActivity::class.java)

        } else {
            intent = Intent(this, HomeOrganizationActivity::class.java)
        }
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
        startActivity(intent)
        finish()
    }

    fun createOrganization(orgName: String?) {
        MyUtils.showProgress(this, flProgress)
        val request = OrganizationRequest()
        request.organization = orgName
        APIOpenAirUtils.createOrganization((application as LidarApp).prefManager!!.getToken(),
            request, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                    getSubType("Organization", orgName)

                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                    MyUtils.toastError(this@AlmostThereActivity, error as ErrorModel)
                }
            })
    }

    fun organizationCheck(orgName: String?) {
        MyUtils.showProgress(this, flProgress)
        val request = OrganizationRequest()
        request.organization = orgName
        APIOpenAirUtils.organizationCheck((application as LidarApp).prefManager!!.getToken(),
            request, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                    val data = result as OrganizationCheckResponse
                    data.isAvailable?.let {
                        if (!it) {
                            createOrganization(orgName)
                            organizationJoinExisting(data.info?.organizationId)
                        } else {
                            Toast.makeText(
                                this@AlmostThereActivity,
                                "Organization not exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                    MyUtils.toastError(this@AlmostThereActivity, error as ErrorModel)
                }
            })
    }

    fun organizationJoinExisting(orgId: String?) {
        MyUtils.showProgress(this, flProgress)
        val request = OrganizationJoinExistRequest()
        request.referenceID = orgId
        APIOpenAirUtils.organizationJoinExist((application as LidarApp).prefManager!!.getToken(),
            request, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                    lineAlmostThere.visibility = View.GONE
                    lineApprovedPending.visibility = View.VISIBLE
                    (application as LidarApp).prefManager!!.setToken("")
                    (application as LidarApp).prefManager!!.setUserId("")
                    (application as LidarApp).prefManager!!.getOrganizationId()?.let {
                        (application as LidarApp).prefManager!!.setOrganizationId("")
                    }
                    (application as LidarApp).prefManager!!.getOrganizationRole()?.let {
                        (application as LidarApp).prefManager!!.setOrganizationRole("")
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                    MyUtils.toastError(this@AlmostThereActivity, error as ErrorModel)
                }
            })
    }


    fun getSubType(type: String, name: String?) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getSubscriptionType(object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as SubscriptionTypeResponse
                data?.body?.rows?.also {
                    for (sub in it) {
                        if (sub.type?.contains(type)!!) {
                            getSubDetailType(sub.subscriptionTypeID, type, name)
                        }
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                MyUtils.toastError(this@AlmostThereActivity, error as ErrorModel)
            }
        })
    }

    fun getSubDetailType(subscriptionTypeID: String?, type: String?, name: String?) {
        APIOpenAirUtils.getSubscription(subscriptionTypeID, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as SubscriptionTypeDetailResponse
                data?.body?.also {
                    for (subType in it) {
                        if (subType.subscriptionName == "Premium") {
                            val subscription = SubscriptionRequest()
                            subscription.subscriptionID = subType.subscriptionID
                            name?.let {
                                subscription.organizationName = it
                            }
                            addSubscription(subscription, type)
                        }
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                MyUtils.toastError(this@AlmostThereActivity, error as ErrorModel)
            }
        })
    }

    fun addSubscription(subscriptionRequest: SubscriptionRequest, type: String?) {
        APIOpenAirUtils.addSubscription((application as LidarApp).prefManager!!.getToken(),
            subscriptionRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    val data = result as SubscriptionResponse
                    (application as LidarApp).prefManager!!.setHasSub(
                        "true"
                    )
                    startHome(type!!.toLowerCase())
                    MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@AlmostThereActivity, flProgress)
                    MyUtils.toastError(this@AlmostThereActivity, error as ErrorModel)
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        (application as LidarApp).prefManager!!.setToken("")
        (application as LidarApp).prefManager!!.setUserId("")
        (application as LidarApp).prefManager!!.getOrganizationId()?.let {
            (application as LidarApp).prefManager!!.setOrganizationId("")
        }
        (application as LidarApp).prefManager!!.getOrganizationRole()?.let {
            (application as LidarApp).prefManager!!.setOrganizationRole("")
        }
        startActivity(intent)
        finish()
    }
}