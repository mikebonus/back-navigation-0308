package com.luxpmsoft.luxaipoc.model.organization

import com.luxpmsoft.luxaipoc.model.BaseResponse
import java.io.Serializable

class OrganizationByNameResponse : BaseResponse() {
    val lists: Organization? = null
}

class OrganizationByNameRequest : Serializable {
    var organization: String? = null
}

class Organization {
    val organizationId: String? = null
    val uid: String? = null
    val name: String? = null
    val subdomain: String? = null
    val logo: String? = null
    val logoKey: String? = null
    val referenceID: String? = null
    val createdAt: String? = null
    val updatedAt: String? = null
}
