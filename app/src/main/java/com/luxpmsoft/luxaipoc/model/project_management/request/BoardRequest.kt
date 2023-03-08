package com.luxpmsoft.luxaipoc.model.project_management.request

import java.io.Serializable

class BoardRequest: Serializable {
    var boardName: String? = null
    var boardType: String? = null
    var orgId: String? = null
}