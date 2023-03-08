package com.luxpmsoft.luxaipoc.model.recentmodel

import java.io.Serializable

class Rows: Serializable {
    var reconstructionID: String? = null
    var modelName: String? = null
    var uploadDateTime: String? = null
    var fileSize: String? = null
    var filePath: String? = null
    var reconstructionType: String? = null
    var sessionID: String? = null
    var thumbnail: String? = null
    var reconstructionModelKey: String? = null
    var thumbnailImageKey: String? = null
    var organizationID: String? = null
    var cadFileID: String? = null
    var uid: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var isCheck: Boolean? = false
    var scanningTypeID: Int? = null
    var cadfilemodel: CadFileModel? = null
    var scanningType: ScanningType? = null
    var bodyPose: BodyPose? = null
}