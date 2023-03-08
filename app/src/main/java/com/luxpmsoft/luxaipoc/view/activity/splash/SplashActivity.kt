package com.luxpmsoft.luxaipoc.view.activity.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.ConstantAPI
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.HomeActivity
import com.luxpmsoft.luxaipoc.view.activity.HomeOrganizationActivity
import com.luxpmsoft.luxaipoc.view.activity.login.SignInActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.SelectAccountTypeActivity
import com.luxpmsoft.luxaipoc.view.activity.welcome.WelcomeActivity

// 2023-03-08-branch2[commit3]
class SplashActivity : AppCompatActivity() {

    companion object{
        var BACK_BUTTON_CLICKED = "no"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_splash)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        val bundle = intent.extras
        if (bundle != null) {
            val email = bundle.get("email")
            val password = bundle.get("password")
            if (email != null && email.toString()
                    .isNotEmpty() && password != null && password.toString().isNotEmpty()
            ) {
                // clear data storage
                (application as LidarApp).prefManager!!.setToken("")
                (application as LidarApp).prefManager!!.setUserId("")
                (application as LidarApp).prefManager?.setTotalNotification("")
                (application as LidarApp).prefManager!!.getOrganizationId()?.let {
                    (application as LidarApp).prefManager!!.setOrganizationId("")
                }
                (application as LidarApp).prefManager!!.getOrganizationRole()?.let {
                    (application as LidarApp).prefManager!!.setOrganizationRole("")
                }
                val intent = Intent(this, SignInActivity::class.java)
                intent.putExtra("email", email.toString())
                intent.putExtra("password", password.toString())
                startActivity(intent)
                finish()
            } else {
                goTo()
            }
        } else {
            goTo()
        }
        zoomIn()
        fadeIn()
    }

    fun goTo() {
        Handler().postDelayed({
            if ((application as LidarApp).prefManager!!.getToken().isNotEmpty()) {
                ConstantAPI.USER_ID = (application as LidarApp).prefManager!!.getUserId()
                if ((application as LidarApp).prefManager?.getHasSub() == "true") {
                    if ((application as LidarApp).prefManager!!.getUserType().isNotEmpty() &&
                        (application as LidarApp).prefManager!!.getUserType()
                            .toLowerCase() == "individual"
                    ) {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, HomeOrganizationActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    val intent = Intent(this, SelectAccountTypeActivity::class.java)
                    startActivity(intent)
                }
                finish()

            } else {
                if (BACK_BUTTON_CLICKED == "yes") {
                    BACK_BUTTON_CLICKED = "no"
                    finishAffinity()
                } else {
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
        }, 1000)
    }

    private fun zoomIn() {
        val image = findViewById<ImageView>(R.id.image_logo)
        val animation: Animation = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.anim_splash_zoom_in
        )
        image.startAnimation(animation)
    }

    private fun fadeIn() {
        val image = findViewById<ImageView>(R.id.image_title)
        val background = findViewById<ImageView>(R.id.background_blur)
        val animation = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.anim_spalsh_fade_in
        )
        background.startAnimation(animation)
        image.startAnimation(animation)
    }

    override fun onBackPressed() {
        BACK_BUTTON_CLICKED = "yes"
        Handler().removeCallbacksAndMessages(null)
        super.onBackPressed()
    }

}

