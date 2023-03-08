package com.luxpmsoft.luxaipoc.model.recentmodel

import java.io.Serializable

class PoseMetaData: Serializable {
    var hips: Double? = null
    var mass: Double? = null
    var chest: Double? = null
    var waist: Double? = null
    var leg_vlen: Double? = null
    var head_vlen: Double? = null
    var torso_vlen: Double? = null
    var height_vlen: Double? = null
    var arm_span_hlen: Double? = null
    var shoulder_hlen: Double? = null
}