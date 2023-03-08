package com.luxpmsoft.luxaipoc.model.repositories

import java.io.Serializable

class RepositoriesModel: Serializable {
    var repositoryId: String? = null
    var uid: String? = null
    var repositoryName: String? = null
    var organizationId: String? = null
    var repositoryPhoto: String? = null
    var isPublic: Boolean? = null
    var time: String? = null
    var repositoryusers: Array<RepositoryUsers>? = null
    var users: ArrayList<OrganizationUser>? = ArrayList()
}