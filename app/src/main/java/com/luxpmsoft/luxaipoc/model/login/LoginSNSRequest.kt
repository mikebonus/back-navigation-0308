package com.luxpmsoft.luxaipoc.model.login

import java.io.Serializable

class LoginSNSRequest: Serializable {
    var email: String? = null
    var first_name: String? = null
    var last_name: String? = null
    var snsType: String? = null
    var snsToken: String? = null
    var profileImage: String? = null
}