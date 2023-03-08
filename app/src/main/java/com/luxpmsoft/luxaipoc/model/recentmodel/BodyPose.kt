package com.luxpmsoft.luxaipoc.model.recentmodel

import java.io.Serializable

class BodyPose: Serializable {
    var bodyPoseID: String? = null
    var reconstructionID: String? = null
    var createdAt: String? = null
    var poseMetaData: PoseMetaData? = null
}