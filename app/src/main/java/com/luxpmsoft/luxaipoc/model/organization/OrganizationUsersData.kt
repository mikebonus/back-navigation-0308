package com.luxpmsoft.luxaipoc.model.organization

import com.luxpmsoft.luxaipoc.model.repositories.OrganizationUser
import java.io.Serializable

class OrganizationUsersData: Serializable {
    var rows: Array<OrganizationUser>? = null
}