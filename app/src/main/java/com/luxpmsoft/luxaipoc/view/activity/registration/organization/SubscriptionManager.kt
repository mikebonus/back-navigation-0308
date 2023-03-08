package com.luxpmsoft.luxaipoc.view.activity.registration.organization

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.home.SubscriptionResponse
import com.luxpmsoft.luxaipoc.model.organization.OrganizationJoinExistRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeDetailResponse
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeResponse
import com.luxpmsoft.luxaipoc.utils.*
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.add_organization.AddOrganizationActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.add_organization.AddOrganizationNameErrorActivity

class SubscriptionManager {

    fun organizationJoinExisting(
        orgId: String?,
        activity: Activity,
        viewProgress: View,
    ) {
        MyUtils.showProgress(activity, viewProgress)
        val request = OrganizationJoinExistRequest()
        request.referenceID = orgId
        val application = activity.application as LidarApp
        APIOpenAirUtils.organizationJoinExist(application.prefManager!!.getToken(),
            request, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(activity, viewProgress)
                    startHomeDestination(activity, AccountType.JoinRequest.type, orgId)
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(activity, viewProgress)
                    MyUtils.toastError(activity, error as ErrorModel)
                }
            })
    }

    fun getSubType(
        type: String,
        name: String?,
        activity: Activity,
        viewProgress: View,
        accountType: String,
        organizationName: String?,
    ) {
        MyUtils.showProgress(activity, viewProgress)
        APIOpenAirUtils.getSubscriptionType(object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as SubscriptionTypeResponse
                data?.body?.rows?.also {
                    for (sub in it) {
                        if (sub.type?.contains(type)!!) {
                            getSubDetailType(
                                sub.subscriptionTypeID,
                                type,
                                name,
                                activity,
                                viewProgress,
                                accountType,
                                organizationName,
                            )
                        }
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(activity, viewProgress)
                MyUtils.toastError(activity, error as ErrorModel)
            }
        })
    }

    private fun getSubDetailType(
        subscriptionTypeID: String?,
        type: String?,
        name: String?,
        activity: Activity,
        viewProgress: View,
        accountType: String,
        organizationName: String?,
    ) {
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
                            addSubscription(
                                subscription,
                                type,
                                activity,
                                viewProgress,
                                accountType,
                                organizationName,
                            )
                        }
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(activity, viewProgress)
                MyUtils.toastError(activity, error as ErrorModel)
            }
        })
    }


    private fun addSubscription(
        subscriptionRequest: SubscriptionRequest,
        type: String?,
        activity: Activity,
        viewProgress: View,
        accountType: String,
        organizationName: String?,
    ) {
        APIOpenAirUtils.addSubscription((activity.application as LidarApp).prefManager!!.getToken(),
            subscriptionRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    val data = result as SubscriptionResponse
                    (activity.application as LidarApp).prefManager!!.setHasSub(
                        "true"
                    )
                    startHomeDestination(
                        activity,
                        accountType,
                        organizationName
                    )
                    MyUtils.hideProgress(activity, viewProgress)
                }

                override fun onError(error: Any?) {
                    startAddOrganizationNameError(activity, organizationName)
                    MyUtils.hideProgress(activity, viewProgress)
                    MyUtils.toastError(activity, error as ErrorModel)
                }
            })
    }

    private fun startHomeDestination(
        activity: Activity,
        accountType: String,
        organizationName: String?
    ) {
        when (accountType) {
            AccountType.Organization.type -> {
                startSuccessCreateAccountActivity(
                    activity,
                    RegistrationFlow.AddOrganization.destination,
                    organizationName
                )
            }
            AccountType.JoinRequest.type -> {
                startSuccessCreateAccountActivity(
                    activity,
                    RegistrationFlow.JoinOrganization.destination,
                    organizationName
                )
            }
            AccountType.Individual.type -> {
                startSuccessCreateAccountActivity(
                    activity,
                    RegistrationFlow.CreatedAccount.destination,
                    organizationName
                )
            }
        }
    }

    private fun startSuccessCreateAccountActivity(
        activity: Activity,
        destination: String,
        organizationName: String?
    ) {
        val intent = Intent(activity, SuccessRegistrationActivity::class.java)
        intent.putExtra(DESTINATION, destination)
        intent.putExtra(ORGANIZATION_NAME, organizationName)
        activity.startActivity(intent)
        activity.finish()
    }

    private fun startAddOrganizationNameError(activity: Activity,organizationName:String?) {
        val intent = Intent(activity, AddOrganizationNameErrorActivity::class.java)
        intent.putExtra(AddOrganizationActivity.ORGANIZATION_NAME, organizationName)
        activity.startActivity(intent)
    }
}