package com.luxpmsoft.luxaipoc.api.workout

import android.util.Log
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.APIService
import com.luxpmsoft.luxaipoc.api.APIUtils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.api.video.APIVideoManager
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ExerciseCategoryResponse
import com.luxpmsoft.luxaipoc.model.workout.WorkoutResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class APIWorkoutUtils {
    companion object {
        const val TAG = "APIWorkoutUtils"

        fun getData(): APIService {
            return APIWorkoutManager.client.create(APIService::class.java)
        }

        fun uploadVideo(accessToken: String?, requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().workoutVideo(accessToken, requestBody)
            Log.i(TAG, call.request().url.toString())
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Log.i(TAG, "Upload video successful")
                        Log.i(TAG, response.body().toString())
                        delegate.onSuccess(response.body())
                    } else {
                        Log.e(TAG, "Upload video failed")
                        Log.i(TAG, response.body().toString())
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun getExerciseVideo(accessToken: String?, pageSize: Int?, pageLimit: Int?,  delegate: APIInterface.onDelegate) {
            val call: Call<WorkoutResponse> = getData().getExerciseVideo(accessToken, pageSize, pageLimit)
            Log.d(TAG, call.request().url.toString())
            call.enqueue(object : Callback<WorkoutResponse> {
                override fun onFailure(call: Call<WorkoutResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<WorkoutResponse>,
                    response: Response<WorkoutResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun getExerciseCategory(accessToken: String?, pageSize: Int?, pageLimit: Int?, delegate: APIInterface.onDelegate) {
            val call: Call<ExerciseCategoryResponse> = getData().getExerciseCategory(accessToken, pageSize, pageLimit)
            Log.d(TAG, call.request().url.toString())
            call.enqueue(object : Callback<ExerciseCategoryResponse> {
                override fun onFailure(call: Call<ExerciseCategoryResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ExerciseCategoryResponse>,
                    response: Response<ExerciseCategoryResponse>
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