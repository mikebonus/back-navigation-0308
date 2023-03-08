package com.luxpmsoft.luxaipoc.model.reconstruction

import java.io.Serializable

class RenameModelRequest: Serializable {
    var reconstructionID : String? = null
    var newModelName : String? = null
}