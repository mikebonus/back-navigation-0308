package com.luxpmsoft.luxaipoc.model.reconstruction

import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import java.io.Serializable

class RenameModelResponse: Serializable {
    var status: String? = null
    var message: String? = null
    var body: Rows? = null
}