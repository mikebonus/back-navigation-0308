package com.luxpmsoft.luxaipoc.model.home

import java.io.Serializable

class SubscriptionData: Serializable {
    var userSubscriptionID: String? = null
    var totalAvailableData: String? = null
    var dataConsumed: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var expiryDate: String? = null
    var subscriptionID: String? = null
    var uid: String? = null
    var paymentID: String? = null
    var organizationID: String? = null
    var user: User? = null
    var subscription: Subscription? = null
}