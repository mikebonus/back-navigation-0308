package com.luxpmsoft.luxaipoc.model.user

import java.io.Serializable

class ChangePasswordRequest: Serializable {
    var oldPassword: String? = null
    var newPassword: String? = null
}