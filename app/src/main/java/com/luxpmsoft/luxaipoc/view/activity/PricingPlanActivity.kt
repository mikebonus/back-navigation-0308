package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.PricingPlanAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.login.LoginResponse
import com.luxpmsoft.luxaipoc.model.recentmodel.ReconstructionResponse
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionType
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeDetailResponse
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import kotlinx.android.synthetic.main.activity_pricing_plan.*
import kotlinx.android.synthetic.main.activity_pricing_plan.flProgress
import kotlinx.android.synthetic.main.activity_recent_models.*

class PricingPlanActivity: BaseActivity() {
    var pricingPlanAdapter: PricingPlanAdapter? = null
    var pricingPlan: ArrayList<SubscriptionType>? = ArrayList()
    var typeIndividual: String? = null
    var typeOrganization: String? = null
    var user: LoginResponse? = null
    var isChoose: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pricing_plan)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        Utils.gridLayoutManager(this, grvPricingPlan, 1, GridLayoutManager.VERTICAL)
        pricingPlanAdapter = PricingPlanAdapter(this, R.layout.item_pricing_plan, pricingPlan!!, object :
            PricingPlanAdapter.OnListener {
            override fun onListener(position: Int, sub: SubscriptionType) {
                pricingPlan?.also {
                    for (sub in it) {
                        sub.isChoose = false
                    }
                }

                pricingPlan?.get(position)?.isChoose = true
                pricingPlanAdapter?.notifyDataSetChanged()
                val subscription = SubscriptionRequest()
                subscription.subscriptionID = sub.subscriptionID
                if (isChoose) {
                    DialogFactory.dialogRenameModel(
                        this@PricingPlanActivity,
                        object : DialogFactory.Companion.DialogListener.RenameModel {
                            override fun renameModel(newName: String) {
                                subscription.organizationName = newName
                                addSubscription(subscription)
                            }
                        }, "Organization Name")
                } else {
                    addSubscription(subscription)
                }
            }
        })
        grvPricingPlan.adapter = pricingPlanAdapter

        val bundle = intent.extras
        //if user go first
        if (bundle == null) {
            getSubType()
        } else {
            //if user from user profile screen
            if((application as LidarApp).prefManager!!.getUser() != null) {
                user = Gson().fromJson((application as LidarApp).prefManager!!.getUser(), LoginResponse::class.java)
            }

            //check hide tab if subscription exist
            distance.visibility = View.GONE
            if (user?.body?.subscription?.subscription_type?.type?.contains("Individual")!!) {
                tvPersonal.visibility = View.VISIBLE
                tvBusiness.visibility = View.GONE
                setBackgroundPersonal()
                isChoose = false
            } else {
                tvPersonal.visibility = View.GONE
                tvBusiness.visibility = View.VISIBLE
                setBackgroundBusiness()
                isChoose = true
            }
            getSubDetailType(user?.body?.subscription?.subscription_type?.subscriptionTypeID, user?.body?.subscription?.subscription_type?.type)
        }
    }

    fun listener() {
        tvPersonal.setOnClickListener {
            isChoose = false
            setBackgroundPersonal()
            typeIndividual?.also {
                pricingPlan?.clear()
                getSubDetailType(typeIndividual, "Individual")
            }
        }

        tvBusiness.setOnClickListener {
            isChoose = true
            setBackgroundBusiness()
            typeOrganization?.also {
                pricingPlan?.clear()
                getSubDetailType(typeOrganization, "Organization")
            }
        }
    }

    fun addSubscription(subscriptionRequest: SubscriptionRequest) {
        MyUtils.showProgress(this@PricingPlanActivity, flProgress)
        APIOpenAirUtils.addSubscription((application as LidarApp).prefManager!!.getToken(),
            subscriptionRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@PricingPlanActivity, flProgress)
                    if (isChoose) {
                        val intent = Intent(this@PricingPlanActivity, HomeOrganizationActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@PricingPlanActivity, HomeActivity::class.java)
                        startActivity(intent)
                    }

                    (application as LidarApp).prefManager!!.setHasSub(
                        "true"
                    )
                    finish()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@PricingPlanActivity, flProgress)
                    MyUtils.toastError(this@PricingPlanActivity, error as ErrorModel)
                }
            })
    }

    fun setBackgroundPersonal() {
        tvPersonal.background = resources.getDrawable(R.drawable.bg_blue_24)
        tvBusiness.background = null
    }

    fun setBackgroundBusiness() {
        tvPersonal.background = null
        tvBusiness.background = resources.getDrawable(R.drawable.bg_blue_24)
    }

    fun getSubType() {
        MyUtils.showProgress(this@PricingPlanActivity, flProgress)
        APIOpenAirUtils.getSubscriptionType(object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@PricingPlanActivity, flProgress)
                val data = result as SubscriptionTypeResponse
                data?.body?.rows?.also {
                    for (sub in it) {
                        if (sub.type?.contains("Individual")!!) {
                            typeIndividual = sub.subscriptionTypeID
                            getSubDetailType(sub.subscriptionTypeID, "Individual")
                            isChoose = false
                        } else if (sub.type?.contains("Organization")!!){
                            typeOrganization = sub.subscriptionTypeID
                        }
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@PricingPlanActivity, flProgress)
                MyUtils.toastError(this@PricingPlanActivity, error as ErrorModel)
            }
        })
    }


    fun getSubDetailType(subscriptionTypeID: String?, type: String?) {
        MyUtils.showProgress(this@PricingPlanActivity, flProgress)
        APIOpenAirUtils.getSubscription(subscriptionTypeID, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@PricingPlanActivity, flProgress)
                val data = result as SubscriptionTypeDetailResponse
                data?.body?.also {
                    for (subType in it) {
                        subType.type = type
                        //select if exist subscription
                        if (user != null) {
                            if (subType.subscriptionID == user?.body?.subscription?.subscriptionID) {
                                subType.isChoose = true
                            }
                        }
                        pricingPlan?.add(subType)
                    }
                }

                pricingPlanAdapter?.notifyDataSetChanged()
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@PricingPlanActivity, flProgress)
                MyUtils.toastError(this@PricingPlanActivity, error as ErrorModel)
            }
        })
    }
}