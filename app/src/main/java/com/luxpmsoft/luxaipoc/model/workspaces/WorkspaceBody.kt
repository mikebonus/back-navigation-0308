package com.luxpmsoft.luxaipoc.model.workspaces

import java.io.Serializable

class WorkspaceBody: Serializable {
    val repositoryId: String? = null
    val organizationId: String? = null
    val repositoryName: String? = null
    val repositoryPhoto: String? = null
    var isPublic: String? = null
    var workspaces: Array<WorkspacesModel>? = null
}