package com.luxpmsoft.luxaipoc.model.recentmodel

import java.io.Serializable

class CadFileRows: Serializable {
    var cadFileID: String? = null
    var description: String? = null
    var name: String? = null
    var fileSize: String? = null
    var boardId: String? = null
    var thumbnail: String? = null
    var createdAt: String? = null
    var unitMeasure: String? = null
    var totalComment: Int? = 0
    var isCheck: Boolean? = null
    var isRemove: Boolean? = null
    var isEdit: Boolean? = null
    var tags: Array<Tags>? = null
    var comments: Array<Comments>? = null
    var reconstructions: Array<Rows>? = null
}