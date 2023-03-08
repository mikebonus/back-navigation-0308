package com.luxpmsoft.luxaipoc.model.workspaces.request

import java.io.Serializable

class WorkspaceRequest: Serializable {
    var workspaceName: String? = null
    var repositoryId: String? = null
    var organizationId: String? = null
    var description: String? = null
}