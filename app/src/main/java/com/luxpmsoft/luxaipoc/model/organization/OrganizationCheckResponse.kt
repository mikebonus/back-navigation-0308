package com.luxpmsoft.luxaipoc.model.organization

import com.luxpmsoft.luxaipoc.model.BaseResponse
import com.luxpmsoft.luxaipoc.model.user.Organization

class OrganizationCheckResponse: BaseResponse() {
    var isAvailable: Boolean? = null
    var info: Organization? = null
    var isMember: String? = null
}