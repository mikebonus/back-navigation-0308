package com.luxpmsoft.luxaipoc.model.defect_detect

class ImagePaths: java.io.Serializable{
    val paths: ArrayList<String>? = null
    val defect_paths: ArrayList<String>? = null
    val nondefect_paths: ArrayList<String>? = null
    var defect_paths_no_bb: ArrayList<String>? = null
}