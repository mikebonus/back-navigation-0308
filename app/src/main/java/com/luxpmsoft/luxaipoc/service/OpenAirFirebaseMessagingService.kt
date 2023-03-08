package com.luxpmsoft.luxaipoc.service

import android.app.*
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.notification.NotificationData
import com.luxpmsoft.luxaipoc.model.notification.NotificationResponse
import com.luxpmsoft.luxaipoc.view.activity.HomeActivity
import com.luxpmsoft.luxaipoc.view.activity.HomeOrganizationActivity
import kotlinx.android.synthetic.main.activity_home_organization.*
import kotlinx.android.synthetic.main.activity_notification.*

class OpenAirFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG: String =
        OpenAirFirebaseMessagingService::class.java.simpleName
    var unread: ArrayList<NotificationData>? = ArrayList()

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.d(
            TAG,
            "notification: >>>>>>>>>>" + Gson().toJson(p0.data.toString())
        )
        Log.d(TAG, "title: " + p0.notification?.title)
        Log.d(TAG, "body: " + p0.notification?.body)

        sendNotification(
            p0.data,
            p0.notification?.body!!,
            p0.notification?.title!!
        )
        sendMessage(p0.notification?.title, p0.notification?.title)
    }

    private fun sendNotification(mapData: Map<String, String>?, body: String, title: String) {
        try{
//            val serviceIntent = Intent(applicationContext, CallNotificationService::class.java)
//            val mBundle = Bundle()
//            mBundle.putString("title", title)
//            mBundle.putString("call_type", body)
//            serviceIntent.putExtras(mBundle)
//            ContextCompat.startForegroundService(applicationContext, serviceIntent)
//            handler?.postDelayed(
//                Runnable {
//                    val iclose = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//                    sendBroadcast(iclose)
//                    stopService(Intent(applicationContext, CallNotificationService::class.java))
//                },
//                TimeUnit.SECONDS.toMillis(2)
//            )
            val intent = Intent(applicationContext, HomeOrganizationActivity::class.java)
            var pendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(applicationContext, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(applicationContext, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_ONE_SHOT)
            }
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, title)
                    .setSmallIcon(R.drawable.icon_app1)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    title,
                    "luxaipoc",
                    NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)

            }
            notificationManager.notify(0, notificationBuilder.build())
            getNotification()
        } catch (e:Exception) {
            e.message
        }
    }

    private fun sendMessage(title: String?, body: String?) {
        val intent = Intent("notification")
        // You can also include some extra data.
        intent.putExtra("title", title)
        intent.putExtra("body", body)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun handleIntent(p0: Intent?) {
        super.handleIntent(p0)
        p0?.extras?.let {
            val bundle: Bundle = it
            if (bundle != null) {
//                val title = bundle.getString("gcm.notification.title", "")
//                val body = bundle.getString("gcm.notification.title", "")
//                sendNotification(null, title, body)
            }
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    fun getNotification() {
        try {
            APIOpenAirUtils.getNotification((application as LidarApp).prefManager?.getToken(), "unread", null, null,
                object : APIInterface.onDelegate {
                    override fun onSuccess(result: Any?) {
                        val data = result as NotificationResponse
//                        data.body?.let {
//                            it.count?.let {
//                                val intent = Intent("total_notification")
//                                intent.putExtra("total_notification", it.toString())
//                                LocalBroadcastManager.getInstance(this@OpenAirFirebaseMessagingService).sendBroadcast(intent)
//                                (application as LidarApp).prefManager?.setTotalNotification(it.toString())
//                            }
//                        }

                        data.body?.rows?.let {
                            for (data in it) {
                                if (data.notificationState.equals("unread")) {
                                    unread?.add(data)
                                    (application as LidarApp).prefManager?.setTotalNotification(unread!!.size.toString())
                                }
                            }
                        }
                    }

                    override fun onError(error: Any?) {
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}