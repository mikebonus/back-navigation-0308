package com.luxpmsoft.luxaipoc.model.workspaces

import java.io.Serializable

class WorkspaceFolderBoard: Serializable {
    var workspaceFolderBoardId: String? = null
    var workspaceFolderId: String? = null
    var workspaceFolderBoardName: String? = null
    var workspaceFolderBoardType: String? = null
    var order: String? = null
    var orgId: String? = null
    var created_at: String? = null
    var updated_at: String? = null
    var total: Int? = 0
    var filegroups: Array<FileGroups>? = null
}