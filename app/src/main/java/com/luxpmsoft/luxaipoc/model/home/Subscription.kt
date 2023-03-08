package com.luxpmsoft.luxaipoc.model.home

import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionType
import com.luxpmsoft.luxaipoc.model.user.Organization
import java.io.Serializable

class Subscription: Serializable {
    var userSubscriptionID: String? = null
    var subscriptionName: String? = null
    var totalAvailableData: String? = null
    var dataConsumed: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var expiryDate: String? = null
    var renewalDate: String? = null
    var subscriptionID: String? = null
    var uid: String? = null
    var paymentID: String? = null
    var organizationID: String? = null
    var subscription_type: SubscriptionType? = null
    var organization: Organization? = null
}