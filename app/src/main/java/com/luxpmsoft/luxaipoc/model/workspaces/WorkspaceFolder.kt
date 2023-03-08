package com.luxpmsoft.luxaipoc.model.workspaces

import java.io.Serializable

class WorkspaceFolder: Serializable {
    var workspaceFolderId: String? = null
    var workspaceId: String? = null
    var workspaceFolderName: String? = null
    var order: String? = null
    var created_at: String? = null
    var boards: Array<WorkspaceFolderBoard>? = null
}