package com.luxpmsoft.luxaipoc.model.notification

import java.io.Serializable

class Notification: Serializable {
    var lat: String? = null
    var lon: String? = null
    var message: String? = null
    var filePath: String? = null
    var modelName: String? = null
    var fileSize: String? = null
    var sessionID: String? = null
    var thumbnail: String? = null
    var messageType: String? = null
    var referenceID: String? = null
    var organizationID: String? = null
    var scanningTypeID: String? = null
    var reconstructionID: String? = null
    var thumbnailImageKey: String? = null
    var reconstructionModelKey: String? = null
}