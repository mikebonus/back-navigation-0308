package com.luxpmsoft.luxaipoc.model.repositories.request

import java.io.Serializable

class PhotoRepositoryRequest: Serializable {
    var organizationId: String? = null
    var repositoryPhoto: String? = null
}