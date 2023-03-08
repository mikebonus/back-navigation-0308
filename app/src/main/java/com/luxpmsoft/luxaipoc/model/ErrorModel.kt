package com.luxpmsoft.luxaipoc.model

import java.io.Serializable

class ErrorModel: Serializable {
    var status: String? = null
    var message: String? = null
    var response: String? = null
}