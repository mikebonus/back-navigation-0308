package com.luxpmsoft.luxaipoc.model.repositories.request

import java.io.Serializable

class AddUserRequest: Serializable {
    var usertoadd: String? = null
    var organizationId: String? = null
}