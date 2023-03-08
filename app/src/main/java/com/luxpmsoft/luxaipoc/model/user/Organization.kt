package com.luxpmsoft.luxaipoc.model.user

import android.graphics.drawable.Drawable
import com.luxpmsoft.luxaipoc.model.repositories.OrganizationUser
import java.io.Serializable

class Organization: Serializable {
    var organizationId: String? = null
    var name: String? = null
    var subdomain: String? = null
    var logo: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var referenceID: String? = null
    var icon: Drawable? = null
    var organizationuser: OrganizationUser? = null
}