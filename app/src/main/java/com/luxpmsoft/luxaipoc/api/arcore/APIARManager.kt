package com.luxpmsoft.luxaipoc.api.arcore

import com.google.gson.GsonBuilder
import com.luxpmsoft.luxaipoc.api.ConstantAPI
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object APIARManager {
    private var retrofit: Retrofit? = null
    var mAccessToken : String? = ""
    val client: Retrofit
        get() {
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor { chain ->
                val original: Request = chain.request()
                val request: Request = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json,zip,application/octet-stream,application/zip,application/x-zip,application/x-zip-compressed")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            val gson = GsonBuilder().setLenient().create()
            val client = OkHttpClient.Builder()
            client.readTimeout(5, TimeUnit.MINUTES)
            client.connectTimeout(5, TimeUnit.MINUTES)
            client.writeTimeout(5, TimeUnit.MINUTES)
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl(ConstantAPI.AR_BASE_URL)
                    .build()
            }
            return retrofit!!
        }
}