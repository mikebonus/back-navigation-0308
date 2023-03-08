package com.luxpmsoft.luxaipoc.model.workspaces.request

import java.io.Serializable

class EditWorkspaceRequest: Serializable {
    var workspaceName: String? = null
    var description: String? = null
}