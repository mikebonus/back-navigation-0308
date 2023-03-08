package com.luxpmsoft.luxaipoc.model.home

import java.io.Serializable

class OrgData: Serializable {
    var role: String? = null
    var organizationId: String? = null
    var organizationUserId: String? = null
    var orgSubscription: Subscription? = null
}