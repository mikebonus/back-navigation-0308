package com.luxpmsoft.luxaipoc.model.recentmodel

import com.luxpmsoft.luxaipoc.model.home.User
import java.io.Serializable

class Comments: Serializable {
    var commentId: String? = null
    var text: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var tagId: String? = null
    var tags: ArrayList<Tags>? = null
    var uid: String? = null
    var user: User? = null
    var comments: Array<Comments>? = null
}