package com.luxpmsoft.luxaipoc.model.workspaces

import java.io.Serializable

class WorkspacesModel: Serializable {
    var workspaceId: String? = null
    var repositoryId: String? = null
    var workspaceName: String? = null
    var description: String? = null
    var created_at: String? = null
    var updated_at: String? = null
    var folders: Array<WorkspaceFolder>? = null
}