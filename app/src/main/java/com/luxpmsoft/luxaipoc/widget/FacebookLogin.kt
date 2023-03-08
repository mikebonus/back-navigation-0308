package com.luxpmsoft.luxaipoc.widget

import java.util.Arrays

import android.content.Intent

import android.app.Activity
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.content.PackageManagerCompat
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.luxpmsoft.luxaipoc.model.ModelResponse
import org.json.JSONException

import androidx.core.content.PackageManagerCompat.LOG_TAG

import com.facebook.GraphResponse

import org.json.JSONObject

import com.facebook.GraphRequest

import com.facebook.AccessToken
import com.facebook.GraphRequest.GraphJSONObjectCallback
import android.os.Bundle
import android.R.id








class FacebookLogin(activity: Activity?) {
    private var callbackManager: CallbackManager = CallbackManager.Factory.create()
    private var activity: Activity? = activity
    private var accessToken: AccessToken? = null

    fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun loginFacebook(listener: Listener.responseListener) {
        if (!checkLoginStatus()) {
            if (activity != null) {
                LoginManager.getInstance()
                    .logInWithReadPermissions(activity!!, Arrays.asList("email", "public_profile", "user_friends"))
            }

            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        // App code
                        val response = ModelResponse()
                        response.statusCode = "0"
                        response.message = "Login with facebook success"
                        //By Profile Class
                        val profile = Profile.getCurrentProfile()
                        if (profile != null) {
                            response.personGivenName = profile.firstName
                            response.personFamilyName = profile.lastName
                            response.personName = profile.name
                            response.personPhoto = profile.getProfilePictureUri(400, 400).toString()
                        }

                        response.accessToken = result.accessToken.token
                        response.userId = result.accessToken.userId
                        accessToken = result.accessToken
                        getUser(response, listener)
                    }


                    override fun onCancel() {
                        // App code
                        val response = ModelResponse()
                        response.statusCode = "-1"
                        response.message = "Login with facebook Cancel"
                        response.accessToken = null
                        response.userId = null
                        listener.onCancel(response)
                    }

                    override fun onError(error: FacebookException) {
                        // App code
                        val response = ModelResponse()
                        response.statusCode = "-1"
                        response.message = "Login with facebook Error"
                        response.accessToken = null
                        listener.onError(response)
                    }
                })
        } else {
            val response = ModelResponse()
            response.statusCode = "0"
            response.message = "Login with facebook success"
            //By Profile Class
            val profile = Profile.getCurrentProfile()
            if (profile != null) {
                response.personGivenName = profile.firstName
                response.personFamilyName = profile.lastName
                response.personName = profile.name
                response.personPhoto = profile.getProfilePictureUri(400, 400).toString()
            }

            response.accessToken = AccessToken.getCurrentAccessToken()?.token
            response.userId = AccessToken.getCurrentAccessToken()?.userId
            accessToken = AccessToken.getCurrentAccessToken()
            getUser(response, listener)
        }
    }

    fun getUser(model: ModelResponse, listener: Listener.responseListener) {
        val request = GraphRequest.newMeRequest(
            accessToken
        ) { `object`, response ->
            try {
                val id = `object`!!.getString("id")
                if (`object`.has("first_name")) {
                    model.personGivenName = `object`.getString("first_name")
                }
                if (`object`.has("last_name")) {
                    model.personFamilyName = `object`.getString("last_name")
                }
                if (`object`.has("name")) {
                    model.personName = `object`.getString("name")
                }
                if (`object`.has("email")) {
                    model.personEmail = `object`.getString("email")
                }
                if (model.personPhoto == null && id != null) {
                    model.personPhoto = "http://graph.facebook.com/$id/picture?type=large"

                }
                listener.onSuccess(model)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val parameters = Bundle()
        parameters.putString(
            "fields",
            "id,email,name,first_name,last_name"
        )

        request.parameters = parameters
        request.executeAsync()
    }

    fun checkLoginStatus(): Boolean {
        val accessToken: AccessToken? = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }

    fun getAccessToken(): AccessToken? {
        return accessToken
    }

    fun logoutFacebook() {
        LoginManager.getInstance().logOut()
    }

    interface Listener {
        interface responseListener {
            fun onSuccess(response: ModelResponse?)
            fun onCancel(response: ModelResponse?)
            fun onError(response: ModelResponse?)
        }

        interface logoutListener {
            fun onLogout()
        }
    }
}
