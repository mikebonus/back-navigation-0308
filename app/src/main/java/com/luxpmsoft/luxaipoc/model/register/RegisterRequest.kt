package com.luxpmsoft.luxaipoc.model.register

import java.io.Serializable

class RegisterRequest: Serializable {
    var password: String? = null
    var first_name: String? = null
    var phone: String? = null
    var email: String? = null
    var last_name: String? = null
}