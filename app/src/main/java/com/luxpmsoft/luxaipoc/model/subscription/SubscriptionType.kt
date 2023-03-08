package com.luxpmsoft.luxaipoc.model.subscription

import java.io.Serializable

class SubscriptionType: Serializable {
    var subscriptionID: String? = null
    var subscriptionTypeID: String? = null
    var subscriptionName: String? = null
    var price: String? = null
    var description: String? = null
    var discount: String? = null
    var totalData: String? = null
    var type: String? = null
    var isChoose: Boolean? = false
}