package com.luxpmsoft.luxaipoc.model.notification

import java.io.Serializable

class NotificationListRequest: Serializable {
    var notificationList: ArrayList<NotificationRequest>? = null
}