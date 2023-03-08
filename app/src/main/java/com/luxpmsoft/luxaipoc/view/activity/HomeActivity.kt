package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
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
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.google.ar.core.CameraConfig
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.common.helpers.CameraPermissionHelper
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.home.SubscriptionResponse
import com.luxpmsoft.luxaipoc.model.login.LoginResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.text.DecimalFormat

class HomeActivity : BaseActivity() {
    var wifiAnimation: AnimationDrawable? = null
    private var session: Session? = null
    var message: String? = null
    private val cameraCapabilities = mutableListOf<CameraCapability>()
    var doubleBackToExitPressedOnce = false
    private var enumerationDeferred: Deferred<Unit>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        animation.setBackgroundResource(R.drawable.animation_usage)
        wifiAnimation = animation.background as AnimationDrawable
        init()
        listener()
    }

    fun init() {
    }

    data class CameraCapability(val camSelector: CameraSelector, val qualities: List<Quality>)

    fun listener() {
        avatarUser.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        rlLogout.setOnClickListener {
//            val intent = Intent(this, WelcomeActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            (application as LidarApp).prefManager!!.setToken("")
//            (application as LidarApp).prefManager!!.setUserId("")
//            startActivity(intent)
//            finish()
        }

        icScan.setOnClickListener {
            showPopupWindow(icScan)
        }

        tvViewModel.setOnClickListener {
            val intent = Intent(this@HomeActivity, RecentModelsActivity::class.java)
            startActivity(intent)
        }

        lnNoSubscription.setOnClickListener {
            MyUtils.openLink(
                this,
                "https://openair3d.luxolis.ai/?mobileToken=" + (application as LidarApp).prefManager!!.getToken()
            )
        }

        lnStorage.setOnClickListener {
            MyUtils.openLink(
                this,
                "https://openair3d.luxolis.ai/?mobileToken=" + (application as LidarApp).prefManager!!.getToken()
            )
        }
    }

    init {
        enumerationDeferred = lifecycleScope.async {
            whenCreated {
                val provider = ProcessCameraProvider.getInstance(this@HomeActivity).get()

                provider.unbindAll()
                for (camSelector in arrayOf(
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    CameraSelector.DEFAULT_FRONT_CAMERA
                )) {
                    try {
                        // just get the camera.cameraInfo to query capabilities
                        // we are not binding anything here.
                        if (provider.hasCamera(camSelector)) {
                            val camera = provider.bindToLifecycle(this@HomeActivity, camSelector)
                            QualitySelector
                                .getSupportedQualities(camera.cameraInfo)
                                .filter { quality ->
                                    listOf(Quality.FHD, Quality.HD, Quality.SD)
                                        .contains(quality)
                                }.also {
                                    cameraCapabilities.add(
                                        CameraCapability(
                                            camSelector,
                                            it
                                        )
                                    )
                                }
                        }
                    } catch (exc: java.lang.Exception) {

                    }
                }
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
                                this@HomeActivity,
                                object : DialogFactory.Companion.DialogListener.Resolution {
                                    override fun resolution(cameraConfig: CameraConfig) {
                                        val intent =
                                            Intent(this@HomeActivity, RawDepthActivity::class.java)
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
                            Intent(this@HomeActivity, RawDepthActivity::class.java)
                        startActivity(intent)
                    }

                    dismiss()
                })

            contentView.findViewById<LinearLayoutCompat>(R.id.lineObject)
                .setOnClickListener(View.OnClickListener {
//                    var quality = ArrayList<String>()
//
//                    for (q in cameraCapabilities[0].qualities) {
//                        quality.add(q.getNameString())
//                    }
//                    DialogFactory.dialogQuality(
//                        this@HomeActivity,
//                        object : DialogFactory.Companion.DialogListener.Quality {
//                            override fun quality(position: Int) {
//
//                            }
//                        },
//                        quality
//                    )
                    val intent = Intent(this@HomeActivity, CameraVideoActivity::class.java)
                    startActivity(intent)
                    dismiss()
                })
            contentView.findViewById<LinearLayoutCompat>(R.id.lineBody)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(this@HomeActivity, CameraVideoActivity::class.java)
                    intent.putExtra("from", "body")
                    startActivity(intent)
                    dismiss()
                })
        }.also { popupWindow ->
            popupWindow.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
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

    override fun onResume() {
        super.onResume()
        getSubscription()
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
                    PackageManager.PERMISSION_GRANTED
                ) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        if (!Environment.isExternalStorageManager()) {
//                            MyUtils.startSettingExternal(this)
//                        }
//                    }
                } else {
                }
                return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        wifiAnimation?.stop()
    }


    override fun onStart() {
        super.onStart()
        wifiAnimation?.start()
    }

    fun getSubscription() {
        MyUtils.showProgress(this@HomeActivity, flProgress)
        APIOpenAirUtils.getSubscriptionUser(
            (application as LidarApp).prefManager!!.getToken(),
            object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@HomeActivity, flProgress)
                    val data = result as SubscriptionResponse
                    data.body?.let {
                        try {
                            tvName.text = "Hi ".plus(data.body?.user?.first_name).plus(",")
                            if (it.subscriptionID == null) {
                                lnDataUsage.visibility = View.GONE
                                lnNoSubscription.visibility = View.VISIBLE
                                lnStorage.visibility = View.GONE
                            } else {
                                val percent =
                                    (data.body?.dataConsumed?.toDouble()!! / data.body?.totalAvailableData?.toDouble()!!)
                                if (percent > 0.8) {
                                    progress_bar_full.max = 100
                                    progress_bar_full.secondaryProgress = 100
                                    progress_bar_full.progress = (percent * 100).toInt()
                                    tvPercent.text =
                                        DecimalFormat("##.##").format(percent * 100).plus("%")
                                    lnDataUsage.visibility = View.GONE
                                    lnNoSubscription.visibility = View.GONE
                                    lnStorage.visibility = View.VISIBLE
                                } else {
                                    progressBar.max = 100
                                    progressBar.secondaryProgress = 100
                                    progressBar.progress = (percent * 100).toInt()
                                    tvUsage.text =
                                        DecimalFormat("##.##").format(data.body?.dataConsumed?.toDouble())
                                            .plus(" GB of ")
                                            .plus(DecimalFormat("##.##").format(data.body?.totalAvailableData?.toDouble()) + " GB Used")
                                    tvPercentData.text =
                                        DecimalFormat("##.##").format(percent * 100).plus("%")
                                    lnDataUsage.visibility = View.VISIBLE
                                    lnNoSubscription.visibility = View.GONE
                                    lnStorage.visibility = View.GONE
                                }
                            }

                            tvPremium.text = it.subscription?.subscriptionName

                            it.user?.profileImageKey.also {
                                MyUtils.loadAvatar(
                                    this@HomeActivity,
                                    (application as LidarApp).prefManager?.getToken(),
                                    it,
                                    avatarUser,
                                    resources.getDrawable(R.drawable.user)
                                )
                            }

                            it.subscription?.also {
                                //save subscription user
                                if ((application as LidarApp).prefManager!!.getUser() != null) {
                                    val user: LoginResponse = Gson().fromJson(
                                        (application as LidarApp).prefManager!!.getUser(),
                                        LoginResponse::class.java
                                    )
                                    user.body?.subscription = it
                                    (application as LidarApp).prefManager!!.setUser(
                                        Gson().toJson(
                                            user
                                        )
                                    )
                                }

                                it.subscriptionName?.let { it1 ->
                                    (application as LidarApp).prefManager!!.setSubscriptionName(
                                        it1
                                    )
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
                    lnView.visibility = View.VISIBLE
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@HomeActivity, flProgress)
                    MyUtils.toastError(this@HomeActivity, error as ErrorModel)
                }
            })
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
}