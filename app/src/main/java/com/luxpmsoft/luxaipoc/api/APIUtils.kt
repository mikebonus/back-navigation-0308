package com.luxpmsoft.luxaipoc.api

import android.util.Log
import com.luxpmsoft.luxaipoc.model.RequestSessionResponse
import com.luxpmsoft.luxaipoc.model.FinishUploadPhotoResponse
import com.luxpmsoft.luxaipoc.model.select.Model3DResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object APIUtils {
    const val TAG = "APIUtils"

    fun getData(): APIService {
        return APIManager.client.create(APIService::class.java)
    }

    //call api login
    fun uploadFile(requestBody: RequestBody, delegate: APIInterface.onDelegate) {

        val call: Call<String> = getData().uploadFile(requestBody)
        call.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("onFailure", "onFailure")
//                var error = ErrorModel()
//                error.message = t.message
                delegate.onError()
            }

            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    delegate.onSuccess(null)
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
    fun getAllModels(delegate: APIInterface.onDelegate) {
        val call: Call<Model3DResponse> = getData().getAllModels()
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
    fun viewModels(sessionId: String, delegate: APIInterface.onDelegate) {
        val call: Call<ResponseBody> = getData().viewModel(sessionId)
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

    fun requestSession(requestBody: RequestBody, delegate: APIInterface.onDelegate) {
        val call: Call<RequestSessionResponse> = getData().requestSession(requestBody)
        Log.d(TAG, "URL: ${call.request().url}")
        call.enqueue(object : Callback<RequestSessionResponse> {
            override fun onFailure(call: Call<RequestSessionResponse>, t: Throwable) {
                Log.e("onFailure", "onFailure")
                delegate.onError()
            }

            override fun onResponse(
                call: Call<RequestSessionResponse>,
                response: Response<RequestSessionResponse>
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

    fun uploadPhoto(requestBody: RequestBody, delegate: APIInterface.onDelegate) {
        val call: Call<ResponseBody> = getData().uploadPhoto(requestBody)
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
                    delegate.onSuccess(response)
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

    fun uploadMutiPhoto(requestBody: RequestBody, delegate: APIInterface.onDelegate) {
        val call: Call<ResponseBody> = getData().uploadMultiPhoto(requestBody)
        Log.d(TAG, call.request().url.toString())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                delegate.onError()
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    delegate.onSuccess(response)
                } else {
                    delegate.onError()
                }
            }
        })
    }

    fun finishPhotoUpload(userId: String, sessionId: String, delegate: APIInterface.onDelegate) {
        val call: Call<FinishUploadPhotoResponse> = getData().finishUploadPhoto(userId, sessionId)
        Log.d(TAG, "URL: ${call.request().url}")
        call.enqueue(object : Callback<FinishUploadPhotoResponse> {
            override fun onFailure(call: Call<FinishUploadPhotoResponse>, t: Throwable) {
                Log.e("onFailure", "onFailure")
                delegate.onError()
            }

            override fun onResponse(
                call: Call<FinishUploadPhotoResponse>,
                response: Response<FinishUploadPhotoResponse>
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