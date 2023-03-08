package com.luxpmsoft.luxaipoc.model.notification

import com.luxpmsoft.luxaipoc.model.home.User
import java.io.Serializable

class NotificationData: Serializable {
    var notificationID: String? = null
    var notificationState: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var user: User? = null
    var notificationData: Notification? = null
}