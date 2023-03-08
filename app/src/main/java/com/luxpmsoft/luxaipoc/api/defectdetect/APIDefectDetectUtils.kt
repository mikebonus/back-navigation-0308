package com.luxpmsoft.luxaipoc.api.defectdetect

import android.util.Log
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.APIService
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.defect_detect.DefectDetectResponse
import com.luxpmsoft.luxaipoc.model.defect_detect.TestedModelsResponse
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModelResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class APIDefectDetectUtils {
    companion object {
        const val TAG = "APIDefectDetectUtils"
        fun getData(): APIService {
            return APIDefectDetectManager.client.create(APIService::class.java)
        }

        //get trained models
        fun getTrainedModels(accessToken: String, userId: String, pageIndex: Int?,pageSize: Int?, delegate: APIInterface.onDelegate) {
            val call: Call<TrainedModelResponse> = getData().getTrainedModel(accessToken, userId, pageIndex, pageSize)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<TrainedModelResponse> {
                override fun onFailure(call: Call<TrainedModelResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<TrainedModelResponse>,
                    response: Response<TrainedModelResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun testMultiDetection(accessToken: String, requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<DefectDetectResponse> = getData().testMultiDetection(accessToken, requestBody)
            Log.d(TAG, call.request().url.toString())
            call.enqueue(object : Callback<DefectDetectResponse> {
                override fun onFailure(call: Call<DefectDetectResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<DefectDetectResponse>,
                    response: Response<DefectDetectResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun increaseDataset(accessToken: String, requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().increaseDataset(accessToken, requestBody)
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

        fun generateSession(accessToken: String, requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().generateSession(accessToken, requestBody)
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

        fun retrainProcess(accessToken: String, requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().retrainProcess(accessToken, requestBody)
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

        fun downloadZip(filePath: String, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().downloadZip(filePath)
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

        fun getTestedModel(accessToken: String, userId: String, session_id: String, pageIndex: Int?,pageSize: Int?, delegate: APIInterface.onDelegate) {
            val call: Call<TestedModelsResponse> = getData().getTestedModels(accessToken, userId, session_id, pageIndex, pageSize)
            Log.d(TAG, call.request().url.toString())
            call.enqueue(object : Callback<TestedModelsResponse> {
                override fun onFailure(call: Call<TestedModelsResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<TestedModelsResponse>,
                    response: Response<TestedModelsResponse>
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

                val jsonObject = JSONObject(response.errorBody()!!.string())
                var responseText = jsonObject.getString("text")
                errorResponse?.let {
                    error.message = responseText
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