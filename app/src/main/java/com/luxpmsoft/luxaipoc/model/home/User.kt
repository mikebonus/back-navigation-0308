package com.luxpmsoft.luxaipoc.model.home

import com.luxpmsoft.luxaipoc.model.user.Organization
import java.io.Serializable

class User: Serializable {
    var full_name: String? = null
    var username: String? = null
    var uid: String? = null
    var first_name: String? = null
    var last_name: String? = null
    var email: String? = null
    var type: String? = null
    var phone: String? = null
    var profileImageKey: String? = null
    var request: String? = null
    var organizationID: String? = null
    var isEmailVerified: String? = null
    var profileImage: String? = null
    var organizations: Array<Organization>? = null
}