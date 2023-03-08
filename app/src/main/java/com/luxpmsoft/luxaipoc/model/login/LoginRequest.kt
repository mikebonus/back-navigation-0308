package com.luxpmsoft.luxaipoc.model.login

import java.io.Serializable

class LoginRequest: Serializable {
    var email: String? = null
    var password: String? = null
}