package com.luxpmsoft.luxaipoc.model.home

import java.io.Serializable

class SubscriptionResponse: Serializable {
    var status: String? = null
    var message: String? = null
    var body: SubscriptionData? = null
}