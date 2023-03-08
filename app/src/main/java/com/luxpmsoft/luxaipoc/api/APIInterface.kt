package com.luxpmsoft.luxaipoc.api

class APIInterface {
    interface onDelegate {
        fun onSuccess(result: Any?)
        fun onError(error: Any? = null)
    }
}