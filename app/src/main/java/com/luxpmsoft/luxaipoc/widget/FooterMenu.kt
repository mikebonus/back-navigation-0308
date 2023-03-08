package com.luxpmsoft.luxaipoc.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.ar.core.CameraConfig
import com.google.ar.core.Session
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.databinding.ItemFooterBinding
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.*

class FooterMenu(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var listener: OnListener? = null
    private var binding: ItemFooterBinding
    private var session: Session? = null

    init {
        binding = ItemFooterBinding.inflate(LayoutInflater.from(context), this, true)
        setupAttributes(attrs)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "ResourceType", "NewApi")
    private fun setupAttributes(attrs: AttributeSet?) {
        val arr = context.theme.obtainStyledAttributes(attrs, R.styleable.Footer, 0, 0)
        binding.lnHome.setOnClickListener {
            updateTabHome((context.applicationContext as LidarApp).prefManager?.getTotalNotification()!!)
            navigateHome()
        }

        binding.ln2.setOnClickListener {
            navigateRecentModel()
            MyUtils.transitionAnimation((context as Activity))
        }

        binding.lnScan.setOnClickListener {
            showPopupWindow(binding.lnScan)
            MyUtils.transitionAnimation((context as Activity))
//            listener?.onClickListener(this)
        }

        binding.lnNotification.setOnClickListener {
            navigateNotification()
            MyUtils.transitionAnimation((context as Activity))
        }

        binding.lnMembers.setOnClickListener {
            navigateProfile()
            MyUtils.transitionAnimation((context as Activity))
        }
    }

    fun updateTabProfile(total: String) {
        MyUtils.visibleView(context as Activity, binding.icMemberActive)
        MyUtils.hideView(context as Activity, binding.ic2Active)
        MyUtils.hideView(context as Activity, binding.icNotificationActive)
        MyUtils.hideView(context as Activity, binding.icHomeActive)
        updateNotification(total)
    }

    fun updateTabNotification(total: String) {
        MyUtils.visibleView(context as Activity, binding.icNotificationActive)
        MyUtils.hideView(context as Activity, binding.ic2Active)
        MyUtils.hideView(context as Activity, binding.icHomeActive)
        MyUtils.hideView(context as Activity, binding.icMemberActive)
        updateNotification(total)
    }

    fun updateTabRecent(total: String) {
        MyUtils.visibleView(context as Activity, binding.ic2Active)
        MyUtils.hideView(context as Activity, binding.icHomeActive)
        MyUtils.hideView(context as Activity, binding.icNotificationActive)
        MyUtils.hideView(context as Activity, binding.icMemberActive)
        updateNotification(total)
    }

    fun updateTabHome(total: String) {
        MyUtils.visibleView(context as Activity, binding.icHomeActive)
        MyUtils.hideView(context as Activity, binding.ic2Active)
        MyUtils.hideView(context as Activity, binding.icNotificationActive)
        MyUtils.hideView(context as Activity, binding.icMemberActive)
        updateNotification(total)
    }

    fun navigateHome() {
        val intent = Intent(context, HomeOrganizationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
        (context as Activity).finish()
        MyUtils.transitionAnimation((context as Activity))
    }

    fun navigateNotification() {
        val intent = Intent(context, NotificationActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateRecentModel() {
        val intent = Intent(context, RecentModelOrganizationActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateProfile() {
        val intent = Intent(context, UserProfileActivity::class.java)
        context.startActivity(intent)
    }

    fun updateNotification(total: String) {
        total?.let {
            binding.tvFileName.text = total
            if (it.isNotEmpty() && it.toInt() > 0) {
                binding.tvFileName.visibility = View.VISIBLE
            } else {
                binding.tvFileName.visibility = View.GONE
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
                                context as Activity,
                                object : DialogFactory.Companion.DialogListener.Resolution {
                                    override fun resolution(cameraConfig: CameraConfig) {
                                        val intent =
                                            Intent(context as Activity, RawDepthActivity::class.java)
                                        intent.putExtra(
                                            "width",
                                            cameraConfig.imageSize.width.toString()
                                        )
                                        intent.putExtra(
                                            "height",
                                            cameraConfig.imageSize.height.toString()
                                        )
                                        context.startActivity(intent)
                                    }
                                },
                                cameraConfigs
                            )
                        } catch (e: Exception) {
                            e.message
                        }
                    } else {
                        val intent =
                            Intent(context as Activity, RawDepthActivity::class.java)
                        context.startActivity(intent)
                    }

                    dismiss()
                })

            contentView.findViewById<LinearLayoutCompat>(R.id.lineObject)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(context as Activity, CameraVideoActivity::class.java)
                    intent.putExtra("from", "object")
                    context.startActivity(intent)
                    dismiss()
                })

            contentView.findViewById<LinearLayoutCompat>(R.id.lineBody)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(context as Activity, CameraVideoActivity::class.java)
                    intent.putExtra("from", "body")
                    context.startActivity(intent)
                    dismiss()
                })

            contentView.findViewById<LinearLayoutCompat>(R.id.lineWorkout)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(context as Activity, CameraVideoActivity::class.java)
                    intent.putExtra("from", "workout")
                    context.startActivity(intent)
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

    fun updateSession(s: Session?) {
        session = s
    }

    fun setOnListener(listener: OnListener) {
        this.listener = listener
    }

    interface OnListener {
        fun onClickListener(view: View)
    }
}