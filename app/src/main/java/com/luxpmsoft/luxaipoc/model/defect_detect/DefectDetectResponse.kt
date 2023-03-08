package com.luxpmsoft.luxaipoc.model.defect_detect

import java.io.Serializable

class DefectDetectResponse: Serializable {
    val defect_count: Int? = null
    val defect_filelist: Array<String>? = null
    val nondefect_filelist: Array<String>? = null
    val full_filelist: Array<String>? = null
    val zip_path: String? = null
}