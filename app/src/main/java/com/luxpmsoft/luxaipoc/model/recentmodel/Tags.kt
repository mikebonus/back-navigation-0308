package com.luxpmsoft.luxaipoc.model.recentmodel

import com.luxpmsoft.luxaipoc.model.home.User
import java.io.Serializable

class Tags: Serializable {
    var tagId: String? = null
    var name: String? = null
    var baseModel: String? = null
    var createdAt: String? = null
    var uid: String? = null
    var position: Position? = null
    var cadFileId: String? = null
    var comments: Array<Comments>? = null
    var user: User? = null
    var isEdit: Boolean? = false
}