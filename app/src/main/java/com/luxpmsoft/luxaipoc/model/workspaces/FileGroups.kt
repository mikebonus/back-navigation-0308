package com.luxpmsoft.luxaipoc.model.workspaces

import java.io.Serializable

class FileGroups: Serializable {
    var fileGroupId: String? = null
    var boardId: String? = null
    var fileSystemGroupName: String? = null
    var subGroupId: String? = null
    var filegrouprecords: Array<FileGroupRecords>? = null
}