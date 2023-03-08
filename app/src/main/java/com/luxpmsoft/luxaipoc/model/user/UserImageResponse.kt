package com.luxpmsoft.luxaipoc.model.user

import com.luxpmsoft.luxaipoc.model.home.User
import java.io.Serializable

class UserImageResponse: Serializable {
    var status: String? = null
    var message: String? = null
    var body: User? = null

}