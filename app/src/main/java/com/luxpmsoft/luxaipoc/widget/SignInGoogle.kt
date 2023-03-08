package com.luxpmsoft.luxaipoc.widget

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.luxpmsoft.luxaipoc.model.GoogleResponse
import java.io.IOException

class SignInGoogle(activity: Activity?) {
    val TAG = SignInGoogle::class.java.simpleName

    private val RC_SIGN_IN = 99
    private var activity: Activity? = activity
    private var account: GoogleSignInAccount? = null
    private var listener: Listener.responseLogin? = null
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()
    private var mGoogleSignInClient: GoogleSignInClient? = GoogleSignIn.getClient(activity!!, gso)

    fun loginWithGoogle(listener: Listener.responseLogin?) {
        this.listener = listener
        if (checkLogin() == null) {
            val signInIntent: Intent = mGoogleSignInClient!!.getSignInIntent()
            activity!!.startActivityForResult(signInIntent, RC_SIGN_IN)
        } else {
            updateUI(account, listener)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?, listener: Listener.responseLogin?) {
        Thread {
            if (account != null) {
                val response = GoogleResponse()
                response.personName = account.displayName
                response.personGivenName = account.givenName
                response.personFamilyName = account.familyName
                response.personEmail = account.email
                response.personId = account.id
                response.personPhoto = account.photoUrl
                response.serverAuthCode = account.serverAuthCode
                val scope = "oauth2:" + Scopes.EMAIL + " " + Scopes.PROFILE
                try {
                    try {
                        response.idToken = GoogleAuthUtil.getToken(activity, account.account, scope)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: GoogleAuthException) {
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    e.message
                }
                listener?.onSuccess(response)
            } else {
                listener?.onError(null)
            }
        }.start()
    }

    private fun checkLogin(): GoogleSignInAccount? {
        account = GoogleSignIn.getLastSignedInAccount(activity)
        return account
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            account = completedTask.getResult(ApiException::class.java)
            //Signed in successfully
            updateUI(account, listener)
        } catch (e: Exception) {
            e.message
            e.printStackTrace()
            listener?.onError("Login Error$e")
            Log.e(TAG, "Login Error$e")
        }
    }

    /*Sign out users*/
    fun signOut(listener: Listener.responseSignOut) {
        mGoogleSignInClient!!.signOut()
            .addOnCompleteListener(
                activity!!,
                OnCompleteListener<Void?> { task -> listener.onSignOut(task) })
    }

    fun signOutGoogle() {
        mGoogleSignInClient!!.signOut()
    }

    /*Disconnect accounts*/
    fun revokeAccess(listener: Listener.responseRevokeAccess) {
        mGoogleSignInClient!!.revokeAccess()
            .addOnCompleteListener(activity!!,
                OnCompleteListener<Void?> { task -> listener.onRevokeAccess(task) })
    }

    interface Listener {
        interface responseLogin {
            fun onSuccess(account: GoogleResponse?)
            fun onError(error: String?)
        }

        interface responseSignOut {
            fun onSignOut(task: Task<Void?>?)
        }

        interface responseRevokeAccess {
            fun onRevokeAccess(task: Task<Void?>?)
        }
    }
}