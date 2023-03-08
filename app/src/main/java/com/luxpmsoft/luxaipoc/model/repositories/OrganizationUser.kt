package com.luxpmsoft.luxaipoc.model.repositories

import com.luxpmsoft.luxaipoc.model.home.User
import java.io.Serializable

class OrganizationUser: Serializable {
    var organizationUserId: String? = null
    var repositoryUserId: String? = null
    var role: String? = null
    var department: String? = null
    var requestAccepted: String? = null
    var organizationId: String? = null
    var user: User? = null
    var isCheck: Boolean? = false
    var pos: Int? = null
}