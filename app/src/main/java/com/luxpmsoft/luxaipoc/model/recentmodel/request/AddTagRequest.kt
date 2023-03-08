package com.luxpmsoft.luxaipoc.model.recentmodel.request

import java.io.Serializable

class AddTagRequest: Serializable {
    var baseModel: String? = null
    var cadFileId: String? = null
    var name: String? = null
    var position: PositionRequest? = null
}