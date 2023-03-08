package com.luxpmsoft.luxaipoc.api.video

import android.util.Log
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.APIService
import com.luxpmsoft.luxaipoc.api.APIUtils
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class APIVideoUtils {
    companion object {
        const val TAG = "APIVideoUtils"

        fun getData(): APIService {
            return APIVideoManager.client.create(APIService::class.java)
        }

        fun uploadMutiVideo(requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().uploadVideo(requestBody)
            Log.d(APIUtils.TAG, call.request().url.toString())
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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
                    }
                }
            })
        }
    }
}