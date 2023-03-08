package com.luxpmsoft.luxaipoc.model.register

import java.io.Serializable

class SendOTPRequest: Serializable {
    var phone: String? = null
    var channel: String? = null
}