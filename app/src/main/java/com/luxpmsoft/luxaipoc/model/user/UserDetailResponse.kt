package com.luxpmsoft.luxaipoc.model.user

import java.io.Serializable

class UserDetailResponse: Serializable {
    var status: String? = null
    var message: String? = null
    var body: UserDetailData? = null
}