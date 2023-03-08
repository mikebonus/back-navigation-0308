package com.luxpmsoft.luxaipoc.model.recentmodel.request

import java.io.Serializable

class CadFileRequest: Serializable {
    var name: String? = null
    var boardId: String? = null
    var description: String? = null
    var reconstructions: ArrayList<String>? = null
}