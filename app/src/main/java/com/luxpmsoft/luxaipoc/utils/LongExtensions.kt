package com.luxpmsoft.luxaipoc.utils

fun Long?.convertByteToMB(): Double {
    val megaByte = (this?.toDouble())?.div(1000000.0)
    return Math.round(megaByte?.times(100.0) ?: 0.0) / 100.00
}
