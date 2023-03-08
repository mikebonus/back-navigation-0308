package com.luxpmsoft.luxaipoc.view.activity

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import com.google.ar.core.Session
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.NotificationAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.notification.NotificationData
import com.luxpmsoft.luxaipoc.model.notification.NotificationListRequest
import com.luxpmsoft.luxaipoc.model.notification.NotificationRequest
import com.luxpmsoft.luxaipoc.model.notification.NotificationResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_notification.*


class NotificationActivity: BaseActivity(), NotificationAdapter.OnListener {
    var notificationAdapter: NotificationAdapter? = null
    var notification: ArrayList<NotificationData>? = ArrayList()
    var earlierAdapter: NotificationAdapter? = null
    var earlier: ArrayList<NotificationData>? = ArrayList()
    var unread: ArrayList<NotificationData>? = ArrayList()
    var pageIndex = 0
    var pageSize = 100
    var total = 0
    var isFirst = false
    private var session: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        Utils.gridLayoutManager(this, grvToday, 1, GridLayoutManager.VERTICAL)
        notificationAdapter = NotificationAdapter(this, R.layout.item_notification, notification!!, this)
        grvToday.adapter = notificationAdapter

        Utils.gridLayoutManager(this, grvEarlier, 1, GridLayoutManager.VERTICAL)
        earlierAdapter = NotificationAdapter(this, R.layout.item_notification, earlier!!, this)
        grvEarlier.adapter = earlierAdapter
        session = MyUtils.createSession(this)
        if((application as LidarApp).prefManager?.getTotalNotification()!!.isEmpty()) {
            tvMaskAllRead.visibility = View.GONE
        }
        getNotification()
    }

    override fun onResume() {
        super.onResume()
        footer.updateTabNotification(unread?.size.toString())
        footer.updateSession(session)
    }

    fun listener() {
        icBack.setOnClickListener {
            finish()
        }

        nestScroll.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(
                v: NestedScrollView,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
//                if (scrollY > oldScrollY) {
//                    if(isFirst && notification?.size!!+earlier!!.size < total) {
//                        isFirst = false
//                        pageIndex++
//                        getNotification()
//                    }
//                }
            }
        })

        tvMaskAllRead.setOnClickListener {
            updateNotification()
        }
    }

    override fun onListener(model: String, position: Int) {

    }

    fun getNotification() {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getNotification((application as LidarApp).prefManager?.getToken(),
            null, pageIndex, pageSize, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@NotificationActivity, flProgress)
                    val data = result as NotificationResponse
                    data.body?.let {
                        total = it.count!!
                        it.rows?.let {
                            for (data in it) {
                                if (MyUtils.compareTwoDate(MyUtils.convertDateTimeISO(data.createdAt.toString()), MyUtils.getCurrentDateTime()) != 0) {
                                    earlier?.add(data)
                                } else {
                                    notification?.add(data)

                                }

                                if (data.notificationState.equals("unread")) {
                                    unread?.add(data)
                                    footer.updateTabNotification(unread?.size.toString())
                                    (application as LidarApp).prefManager?.setTotalNotification(unread!!.size.toString())

                                    if(unread!!.isEmpty()) {
                                        tvMaskAllRead.visibility = View.GONE
                                    } else {
                                        tvMaskAllRead.visibility = View.VISIBLE
                                    }
                                    try {
                                        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                                        val r = RingtoneManager.getRingtone(
                                            applicationContext,
                                            notification
                                        )
                                        r.play()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                }
                            }
                        }
                        notificationAdapter?.notifyDataSetChanged()
                        earlierAdapter?.notifyDataSetChanged()
                    }

                    notification?.let {
                        if (it.isNotEmpty()) {
                            grvToday.visibility = View.VISIBLE
                            tvEmptyToday.visibility = View.GONE
                        } else {
                            grvToday.visibility = View.GONE
                            tvEmptyToday.visibility = View.VISIBLE
                        }
                    }
                    earlier?.let {
                        if (it.isNotEmpty()) {
                            grvEarlier.visibility = View.VISIBLE
                            tvEmptyEarlier.visibility = View.GONE
                        } else {
                            grvEarlier.visibility = View.GONE
                            tvEmptyEarlier.visibility = View.VISIBLE
                        }
                    }
                    if (!isFirst) {
                        isFirst = true
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@NotificationActivity, flProgress)
                    MyUtils.toastError(this@NotificationActivity, error as ErrorModel)
                }
            })
    }

    fun updateNotification() {
        MyUtils.showProgress(this, flProgress)
        val notificationListRequest = NotificationListRequest()
        val notificationList: ArrayList<NotificationRequest> = ArrayList()
        for (data in earlier!!) {
            if (data.notificationState.toString().contains("unread")) {
                val notificationRequest = NotificationRequest()
                notificationRequest.notificationID = data.notificationID
                notificationRequest.notificationState = "read"
                notificationList?.add(notificationRequest)
            }
        }
        for (data in notification!!) {
            if (data.notificationState.toString().contains("unread")) {
                val notificationRequest = NotificationRequest()
                notificationRequest.notificationID = data.notificationID
                notificationRequest.notificationState = "read"
                notificationList?.add(notificationRequest)
            }
        }
        notificationListRequest.notificationList = notificationList
        APIOpenAirUtils.updateNotification((application as LidarApp).prefManager!!.getToken(), notificationListRequest, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@NotificationActivity, flProgress)
                (application as LidarApp).prefManager?.setTotalNotification("")
                pageIndex = 0
                notification?.clear()
                earlier?.clear()
                footer.updateTabNotification((application as LidarApp).prefManager?.getTotalNotification().toString())
                if((application as LidarApp).prefManager?.getTotalNotification()!!.isEmpty()) {
                    tvMaskAllRead.visibility = View.GONE
                }
                getNotification()
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@NotificationActivity, flProgress)
                MyUtils.toastError(this@NotificationActivity, error as ErrorModel)
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        session?.let {
            session = null
        }
    }
}