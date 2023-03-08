package com.luxpmsoft.luxaipoc.api.arcore

import android.util.Log
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.APIService
import com.luxpmsoft.luxaipoc.model.select.Model3DResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class APIARUtils {
    companion object {
        const val TAG = "APIARUtils"

        fun getData(): APIService {
            return APIARManager.client.create(APIService::class.java)
        }

        fun uploadFileZip(requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().uploadFileZip("true",requestBody)
            Log.d(TAG, call.request().url.toString())
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("onFailure", "onFailure")
//                var error = ErrorModel()
//                error.message = t.message
                    delegate.onError()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        delegate.onError()
                        Log.e("onFail", "onFail")
//                    val errorResponse = Gson().fromJson(
//                        response.errorBody()!!.string(),
//                        ErrorModel::class.java
//                    )
//                    var error = ErrorModel()
//                    error.message = errorResponse.message
//                    error.status = response.code().toString()
//                    delegate.onError(error)
                    }
                }
            })
        }

        //get all model
        fun getAllModels(userId: String, delegate: APIInterface.onDelegate) {
            val call: Call<Model3DResponse> = getData().getArAllModels(userId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<Model3DResponse> {
                override fun onFailure(call: Call<Model3DResponse>, t: Throwable) {
                    Log.e("onFailure", "onFailure")
                    delegate.onError()
                }

                override fun onResponse(
                    call: Call<Model3DResponse>,
                    response: Response<Model3DResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        delegate.onError()
                        Log.e("onFail", "onFail")
                    }
                }
            })
        }

        //view model
        fun viewArModels(userId: String, sessionId: String, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().viewArModel(userId, sessionId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("onFailure", "onFailure")
                    delegate.onError()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        delegate.onError()
                        Log.e("onFail", "onFail")
                    }
                }
            })
        }


        //view model
        fun getModelUrl(userId: String, sessionId: String, delegate: APIInterface.onDelegate) {
            val call: Call<String> = getData().getModelUrl(userId, sessionId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("onFailure", "onFailure")
                    delegate.onError()
                }

                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        delegate.onError()
                        Log.e("onFail", "onFail")
                    }
                }
            })
        }

        //view model
        fun getThumbnail(userId: String, sessionId: String, delegate: APIInterface.onDelegate) {
            val call: Call<String> = getData().getThumbnail(userId, sessionId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("onFailure", "onFailure")
                    delegate.onError()
                }

                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        delegate.onError()
                        Log.e("onFail", "onFail")
                    }
                }
            })
        }

        //rename model
        fun renameModel(userId: String, sessionId: String, newSessionId: String, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().renameModel(userId, sessionId, newSessionId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("onFailure", "onFailure")
                    delegate.onError()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        delegate.onError()
                        Log.e("onFail", "onFail")
                    }
                }
            })
        }
    }
}