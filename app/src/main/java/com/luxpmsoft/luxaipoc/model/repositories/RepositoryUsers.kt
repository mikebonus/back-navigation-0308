package com.luxpmsoft.luxaipoc.model.repositories

import java.io.Serializable

class RepositoryUsers: Serializable {
    var id: String? = null
    var organizationUserId: String? = null
    var repositoryId: String? = null
    var assignedBy: String? = null
    var organizationuser: OrganizationUser? = null
    var created_at: String? = null
}