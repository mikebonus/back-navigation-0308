package com.luxpmsoft.luxaipoc.widget

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider

class SignInApple(activity: Activity) {
    val TAG = SignInApple::class.java.simpleName
    private var activity: Activity? = activity
    private var provider = OAuthProvider.newBuilder("apple.com")
    private var mAuth = FirebaseAuth.getInstance()
    fun init(listener: Listener.responseListener) {
        provider.setScopes(arrayOf("email", "name").toMutableList())
        provider.addCustomParameter("locale", "vi");
        val pending = mAuth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                listener.onSuccess(authResult.user?.getIdToken(true)?.result?.token!!, authResult.user?.uid!!)
            }.addOnFailureListener { e ->
                Log.e(TAG, "checkPending:onFailure", e)
                listener.onError("checkPending:onFailure")
            }
        } else {
            mAuth.startActivityForSignInWithProvider(activity!!, provider.build())
                .addOnSuccessListener { authResult ->
                    // Sign-in successful!
                    val user = authResult.user
                    user!!.getIdToken(true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val idToken: String = task.result!!.getToken()!!
                                listener.onSuccess(idToken, user.uid)
                            } else {
                                listener.onError("activitySignIn:onFailuregetToken")
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "activitySignIn:onFailure", e)
                    listener.onError("activitySignIn:onFailure")
                }
        }
    }

    fun signOut() {
        mAuth.signOut()
    }

    interface Listener {
        interface responseListener {
            fun onSuccess(token: String, uuid: String)
            fun onError(string: String)
        }
    }
}