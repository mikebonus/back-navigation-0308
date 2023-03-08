package com.luxpmsoft.luxaipoc

import android.app.Application
import androidx.multidex.MultiDex
import com.facebook.FacebookSdk
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.luxpmsoft.luxaipoc.utils.PrefManager

class LidarApp : Application() {
    var prefManager: PrefManager? = null
    var auth: FirebaseAuth? = null
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        FacebookSdk.sdkInitialize(applicationContext)
        prefManager = PrefManager(this)
        auth = FirebaseAuth.getInstance()
        MultiDex.install(this)
    }
}