package com.luxpmsoft.luxaipoc.model.defect_detect

import java.io.Serializable

class Annotation(val name: String? = null,
                 val classname: Float? = null,
                 val x: Float? = null,
                 val y: Float? = null,
                 val width: Float? = null,
                 val height: Float? = null): Serializable {

}