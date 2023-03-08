package com.luxpmsoft.luxaipoc.model.notification

import java.io.Serializable

class NotificationBody: Serializable {
    val count: Int? = null
    val rows: Array<NotificationData>? = null
}