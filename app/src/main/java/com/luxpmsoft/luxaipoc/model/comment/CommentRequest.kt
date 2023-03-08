package com.luxpmsoft.luxaipoc.model.comment

import java.io.Serializable

class CommentRequest: Serializable {
    var text: String? = null
    var cadFileId: String? = null
    var parentCommentId: String? = null
}