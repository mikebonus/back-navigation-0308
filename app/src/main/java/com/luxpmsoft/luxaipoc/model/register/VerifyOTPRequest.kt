package com.luxpmsoft.luxaipoc.model.register

import java.io.Serializable

class VerifyOTPRequest: Serializable {
    var phone: String? = null
    var code: String? = null
}