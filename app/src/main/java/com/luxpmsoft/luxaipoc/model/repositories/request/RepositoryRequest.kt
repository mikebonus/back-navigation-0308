package com.luxpmsoft.luxaipoc.model.repositories.request

import java.io.Serializable

class RepositoryRequest: Serializable {
    var repositoryName: String? = null
    var organizationId: String? = null
    var repositoryPhoto: String? = null
    var isPublic: String? = null
}