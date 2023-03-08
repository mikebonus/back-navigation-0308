package com.luxpmsoft.luxaipoc.model.user

import com.luxpmsoft.luxaipoc.model.home.OrgData
import com.luxpmsoft.luxaipoc.model.home.Subscription
import com.luxpmsoft.luxaipoc.model.home.User
import java.io.Serializable

class UserDetailData: Serializable {
    var user: User? = null
    var userSubscription: Subscription? = null
    var orgData: Array<OrgData>? = null
}