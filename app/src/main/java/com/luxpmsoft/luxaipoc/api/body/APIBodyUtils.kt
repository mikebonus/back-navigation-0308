package com.luxpmsoft.luxaipoc.api.body

import android.util.Log
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.APIService
import com.luxpmsoft.luxaipoc.api.APIUtils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.api.video.APIVideoUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.RequestSessionResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class APIBodyUtils {
    companion object {
        const val TAG = "APIBodyUtils"

        fun getData(): APIService {
            return APIBodyManager.client.create(APIService::class.java)
        }

        fun createSession(delegate: APIInterface.onDelegate) {
            val call: Call<RequestSessionResponse> = getData().createSession()
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<RequestSessionResponse> {
                override fun onFailure(call: Call<RequestSessionResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<RequestSessionResponse>,
                    response: Response<RequestSessionResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun uploadVideo(requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().uploadVideo(requestBody)
            Log.d(TAG, call.request().url.toString())
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //download body
        fun downloadBody(sessionId: String, delegate: APIInterface.onDelegate) {
            val call: Call<String> = getData().downloadBody(sessionId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun handleError(response : Response<out Any>?, delegate: APIInterface.onDelegate, message: String? = null) {
            var error = ErrorModel()
            try {
                val errorResponse = Gson().fromJson(
                    response?.errorBody()!!.string(),
                    ErrorModel::class.java
                )
                errorResponse?.let {
                    if (errorResponse.message != null) {
                        error.message = errorResponse.message
                    } else {
                        errorResponse?.response?.let {
                            error.message = it
                        }
                    }
                    error.status = response.code().toString()
                }

                if (error.message == null) {
                    if (message != null) {
                        error.message = message
                    } else {
                        error.message = "Error"
                    }
                }
                delegate.onError(error)
            } catch (e: Exception) {
                if (response != null && response.message() != null) {
                    error.message = response.message()
                } else {
                    error.message = "Error"
                }
                message?.let {
                    error.message = message
                }

                delegate.onError(error)
            }
        }
    }
}