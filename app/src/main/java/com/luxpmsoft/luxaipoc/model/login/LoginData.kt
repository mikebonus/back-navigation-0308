package com.luxpmsoft.luxaipoc.model.login

import com.luxpmsoft.luxaipoc.model.home.Subscription
import java.io.Serializable

class LoginData: Serializable {
    var id: String? = null
    var type: String? = null
    var subscriptionType: String? = null
    var first_name: String? = null
    var last_name: String? = null
    var full_name: String? = null
    var email: String? = null
    var subscriptionName: String? = null
    var profileImageKey: String? = null
    var hasSub: String? = null
    var customerHasSub: String? = null
    var orgHasSub: String? = null
    var remainingData: String? = null
    var totalAvailableData: String? = null
    var dataConsumed: String? = null
    var organizationId: String? = null
    var organizationRole: String? = null
    var organizationUserId: String? = null
    var organizationName: String? = null
    var threeD: Token? = null
    var subscription: Subscription? = null
}