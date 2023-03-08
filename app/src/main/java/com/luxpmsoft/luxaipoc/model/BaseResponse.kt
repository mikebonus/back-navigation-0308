package com.luxpmsoft.luxaipoc.model

import java.io.Serializable

open class BaseResponse: Serializable {
    var status: String? = null
    var message: String? = null
}