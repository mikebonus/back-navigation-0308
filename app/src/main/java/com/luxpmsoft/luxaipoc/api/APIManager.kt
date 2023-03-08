package com.luxpmsoft.luxaipoc.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object APIManager {
    private var retrofit: Retrofit? = null
    var mAccessToken : String? = ""
    val client: Retrofit
        get() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val httpClient = OkHttpClient.Builder()
            httpClient
                .addInterceptor(interceptor)
                .addInterceptor { chain ->
                val original: Request = chain.request()
                val request: Request = original.newBuilder()
                    .header("Authorization", "Bearer $mAccessToken")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            val gson = GsonBuilder().setLenient().create()

//            val trustManagers: Array<TrustManager> = Utils.checkCert(ConstantAPI.context!!)!!.getTrustManagers()
//            val x509TrustManager: X509TrustManager = trustManagers[0] as X509TrustManager
//            // Create an SSLContext that uses our TrustManager
//            val context: SSLContext = SSLContext.getInstance("TLS")
//            context.init(null, Utils.checkCert(ConstantAPI.context!!)!!.getTrustManagers(), null)
//            //create Okhttp client
//
//            val sslSocketFactory: SSLSocketFactory = context.getSocketFactory()
            val client = OkHttpClient.Builder()
            client.readTimeout(60, TimeUnit.SECONDS)
            client.connectTimeout(60, TimeUnit.SECONDS)
//            client.sslSocketFactory(sslSocketFactory, x509TrustManager)
//            client.hostnameVerifier(object : HostnameVerifier {
//                override fun verify(hostname: String?, session: SSLSession?): Boolean {
//                    return true
//                }
//            })
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl(ConstantAPI.BASE_URL)
                    .build()
            }
            return retrofit!!
        }
}