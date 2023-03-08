package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.ar.core.CameraConfig
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.MySitesAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.defectdetect.APIDefectDetectUtils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.common.helpers.CameraPermissionHelper
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModelResponse
import com.luxpmsoft.luxaipoc.model.home.OrganizationDashboardResponse
import com.luxpmsoft.luxaipoc.model.login.LoginResponse
import com.luxpmsoft.luxaipoc.model.notification.NotificationData
import com.luxpmsoft.luxaipoc.model.notification.NotificationResponse
import com.luxpmsoft.luxaipoc.model.user.MeResponse
import com.luxpmsoft.luxaipoc.model.user.Organization
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import com.luxpmsoft.luxaipoc.widget.DropdownSelect
import com.luxpmsoft.luxaipoc.widget.FooterMenu
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_home_organization.*
import kotlinx.android.synthetic.main.item_dropdown_select.view.*
import java.text.DecimalFormat
import kotlin.math.round

class HomeOrganizationActivity: BaseActivity(), MySitesAdapter.OnListener, FooterMenu.OnListener,
    DropdownSelect.OnListener{
    var user: LoginResponse? = null
    private var session: Session? = null
    var message: String? = null
    var isFirst = false
    var doubleBackToExitPressedOnce = false
    var unread: ArrayList<NotificationData>? = ArrayList()
    val dropdownList: ArrayList<Organization> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_organization)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            IntentFilter("notification")
        )
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val title = intent.getStringExtra("title")
            val body = intent.getStringExtra("body")
            title?.let {
                Alerter.create(this@HomeOrganizationActivity)
                    .setTitle(title)
                    .setText(body.toString())
                    .setBackgroundColorRes(R.color.blue1)
                    .setDuration(2000)
                    .show()
            }
        }
    }

    fun init() {
        tvTextGood.text = MyUtils.getTextGood(this)

        runOnUiThread {
            getMe()
            getNotification()
            getTrainedModels()
        }
    }

    fun listener() {
        spnSites.setOnListener(this)
        footer.setOnListener(this)
        lnViewModel.setOnClickListener {
            val intent = Intent(this@HomeOrganizationActivity, RecentModelOrganizationActivity::class.java)
            startActivity(intent)
        }

        lineProfile.setOnClickListener {
//            val intent = Intent(this, UserProfileActivity::class.java)
//            startActivity(intent)
        }
        lineRepositories.setOnClickListener {
            val intent = Intent(this, RepositoriesActivity::class.java)
            startActivity(intent)
        }

        lineDefectDetect.setOnClickListener {
            //delete all file retrain exists
            val mediaDir = getExternalFilesDir(Environment.DIRECTORY_DCIM)
            mediaDir?.let {
                if (it.exists()) {
                    for (file in it.listFiles()) {
                        if (file.exists()) {
                            for (subFile in file.listFiles()) {
                                subFile.delete()
                            }
                            file.delete()
                        }
                    }
                }
            }
            val intent = Intent(this, DefectDetectionModelsActivity::class.java)
            startActivity(intent)
            MyUtils.transitionAnimation(this)
        }
    }

    override fun onResume() {
        super.onResume()

        if (dropdownList.isNotEmpty()) {
            spnSites.setSelection((application as LidarApp).prefManager?.getOrganizationId())
        }
        if (session == null) {
            var exception: Exception? = null
            try {
                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this)
                    return
                }

                // Creates the ARCore session.
                session = Session( /* context= */this)
                if (!session?.isDepthModeSupported(Config.DepthMode.RAW_DEPTH_ONLY)!!) {
                    message = "This device does not support the ARCore Raw Depth API. See" +
                            "https://developers.google.com/ar/devices for a list of devices that do."
                }
            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableUserDeclinedInstallationException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update this app"
                exception = e
            } catch (e: UnavailableDeviceNotCompatibleException) {
                message = "This device does not support AR"
                exception = e
            } catch (e: Exception) {
                message = "Failed to create AR session"
                exception = e
            }
        }

        if((application as LidarApp).prefManager!!.getUser() != null) {
            user = Gson().fromJson((application as LidarApp).prefManager!!.getUser(), LoginResponse::class.java)
            if ((application as LidarApp).prefManager!!.getFullName() != null) {
                tvName.text = (application as LidarApp).prefManager!!.getFullName()
            } else {
                tvName.text = user?.body?.full_name
            }
            MyUtils.loadAvatar(this@HomeOrganizationActivity, (application as LidarApp).prefManager?.getToken(),
                (application as LidarApp).prefManager!!.getProfileImageKey(), avatarUser, resources.getDrawable(R.drawable.user))
        }
    }

    @SuppressLint("NewApi")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        if (!Environment.isExternalStorageManager()) {
//                            MyUtils.startSettingExternal(this)
//                        }
                    }
                } else {
                }
                return
            }
        }
    }

    private fun showPopupWindow(anchor: View) {
        PopupWindow(anchor.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor.context)
            contentView = inflater.inflate(R.layout.dialog_choose_scan, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }
            contentView.findViewById<LinearLayoutCompat>(R.id.lineScene)
                .setOnClickListener(View.OnClickListener {
                    if (session != null) {
                        try {
                            val cameraConfigs: ArrayList<CameraConfig> = ArrayList<CameraConfig>()
                            for (i in session?.supportedCameraConfigs?.indices!!) {
                                cameraConfigs.add(session!!.supportedCameraConfigs[i])
                            }
                            DialogFactory.dialogResolution(
                                this@HomeOrganizationActivity,
                                object : DialogFactory.Companion.DialogListener.Resolution {
                                    override fun resolution(cameraConfig: CameraConfig) {
                                        val intent =
                                            Intent(this@HomeOrganizationActivity, RawDepthActivity::class.java)
                                        intent.putExtra(
                                            "width",
                                            cameraConfig.imageSize.width.toString()
                                        )
                                        intent.putExtra(
                                            "height",
                                            cameraConfig.imageSize.height.toString()
                                        )
                                        startActivity(intent)
                                    }
                                },
                                cameraConfigs
                            )
                        } catch (e: Exception) {
                            e.message
                        }
                    } else {
                        val intent =
                            Intent(this@HomeOrganizationActivity, RawDepthActivity::class.java)
                        startActivity(intent)
                    }

                    dismiss()
                })

            contentView.findViewById<LinearLayoutCompat>(R.id.lineObject)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(this@HomeOrganizationActivity, CameraVideoActivity::class.java)
                    intent.putExtra("from", "object")
                    startActivity(intent)
                    dismiss()
                })

            contentView.findViewById<LinearLayoutCompat>(R.id.lineBody)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(this@HomeOrganizationActivity, CameraVideoActivity::class.java)
                    intent.putExtra("from", "body")
                    startActivity(intent)
                    dismiss()
                })

        }.also { popupWindow ->
            popupWindow.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT)
            )

            // Absolute location of the anchor view
            val location = IntArray(2).apply {
                anchor.getLocationOnScreen(this)
            }
            val size = Size(
                popupWindow.contentView.measuredWidth,
                popupWindow.contentView.measuredHeight
            )
            popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.CENTER,
                0,
                location[1] - size.height
            )
        }
    }

    fun getOrganizationDashboard(organizationId: String?) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getDashboard(organizationId,
            (application as LidarApp).prefManager!!.getToken(), object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@HomeOrganizationActivity, flProgress)
                val data = result as OrganizationDashboardResponse
                if (!isFirst) {
                    isFirst = true
                }

                data.body?.let {
                    try {
//                        tvTotalObject.text = it.modelCount.toString()
                        tvProject.text = it.nbrFolders.toString()
                        tvBoards.text = it.nbrBoards.toString()
                        tvFiles.text = it.nbrFiles.toString()
                        progressBar.max = round(it.organisationSubDetails?.totalAvailableData?.toDouble()!!).toInt()
                        progressBar.secondaryProgress = round(it.organisationSubDetails?.totalAvailableData?.toDouble()!!).toInt()
                        progressBar.progress = round(it.organisationSubDetails?.dataConsumed?.toDouble()!!).toInt()
                        tvTotalData.text = it.organisationSubDetails?.totalAvailableData.plus(" GB *")
                        val percent = (it.organisationSubDetails?.dataConsumed?.toDouble()!! / it.organisationSubDetails?.totalAvailableData?.toDouble()!!)
                        tvUsage.text = DecimalFormat("##.##").format(it.organisationSubDetails.dataConsumed?.toDouble()).plus(" GB ")
//                            .plus(DecimalFormat("##.##").format(it.organisationSubDetails.totalAvailableData?.toDouble())+" GB Used")
                        tvPercentData.text = DecimalFormat("##.##").format(percent*100)+"%"

                        it.organisationSubDetails?.subscription?.also {
                            //save subscription user
                            if((application as LidarApp).prefManager!!.getUser() != null) {
                                val user: LoginResponse = Gson().fromJson((application as LidarApp).prefManager!!.getUser(), LoginResponse::class.java)
                                user.body?.subscription = it
                                (application as LidarApp).prefManager!!.setUser(Gson().toJson(user))
                            }
                            it.subscriptionName?.let { it1 ->
                                (application as LidarApp).prefManager!!.setSubscriptionName(
                                    it1
                                )
                                tvSubscriptionName.text = it1
                            }
                            it.subscription_type?.type?.also {
                                (application as LidarApp).prefManager!!.setUserType(
                                    it.toLowerCase()
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.message
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@HomeOrganizationActivity, flProgress)
                MyUtils.toastError(this@HomeOrganizationActivity, error as ErrorModel)
            }
        })
    }

    fun getMe() {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getMe((application as LidarApp).prefManager!!.getToken() , object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val me = result as MeResponse
                dropdownList.clear()
                me.body?.organizations?.let {
                    for (site in it) {
                        if (site.organizationuser?.requestAccepted == "true") {
                            dropdownList.add(site)
                        }

                        if (site.organizationId == (application as LidarApp).prefManager!!.getOrganizationId()) {
                            site.organizationuser?.role?.let { it1 ->
                                (application as LidarApp).prefManager?.setOrganizationRole(
                                    it1
                                )
                            }
                        }
                    }
                }
                if (dropdownList.size > 0) {
                    spnSites.setData(dropdownList)
                    dropdownList[0].organizationId?.let {
                        (application as LidarApp).prefManager?.setOrganizationId(
                            it
                        )
                        spnSites.setSelection((application as LidarApp).prefManager?.getOrganizationId())
                        getOrganizationDashboard((application as LidarApp).prefManager?.getOrganizationId())
                    }
                } else {
                    val intent = Intent(this@HomeOrganizationActivity, HomeActivity::class.java)
                    intent.putExtra("pending", "pending")
                    startActivity(intent)
                    finish()
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@HomeOrganizationActivity, flProgress)
                MyUtils.toastError(this@HomeOrganizationActivity, error as ErrorModel)
            }
        })
    }

    fun getTrainedModels() {
        APIDefectDetectUtils.getTrainedModels((application as LidarApp).prefManager!!.getToken(),
            (application as LidarApp).prefManager!!.getUserId(), 1, 1, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as TrainedModelResponse
                data.total_data?.let {
                    tvTotalObject.text = it.toString()
                }
            }

            override fun onError(error: Any?) {
                tvTotalObject.text = "0"
            }
        })
    }

    override fun onListener(model: String, position: Int) {
        val intent = Intent(this, RepositoriesActivity::class.java)
        startActivity(intent)
    }

    override fun onClickListener(view: View) {
        showPopupWindow(view)
    }

    override fun onChoose(view: View, id: String?) {
        if (isFirst) {
            id?.let {
                (application as LidarApp).prefManager?.setOrganizationId(
                    it
                )
                getOrganizationDashboard(it)
            }
        }

        if (dropdownList.size <= 1) {
            view.lnDropDownArrow.visibility =View.GONE
        } else {
            view.lnDropDownArrow.visibility =View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity()
            return
        }

        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click back again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    fun getNotification() {
        APIOpenAirUtils.getNotification((application as LidarApp).prefManager?.getToken(),
            "unread", 0, 90, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    val data = result as NotificationResponse
                    data.body?.rows?.let {
                        for (data in it) {
                            if (data.notificationState.equals("unread")) {
                                unread?.add(data)
                                footer.updateTabHome(unread!!.size.toString())
                                (application as LidarApp).prefManager?.setTotalNotification(unread!!.size.toString())
                            }
                        }
                    }
                    if (!isFirst) {
                        isFirst = true
                    }
                }

                override fun onError(error: Any?) {
                }
            })
    }
}
