package com.luxpmsoft.luxaipoc.view.activity.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.databinding.ActivityVerifyEmailBinding
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.register.EmailVerificationRequest
import com.luxpmsoft.luxaipoc.model.register.EmailVerificationResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.openMailApp
import com.luxpmsoft.luxaipoc.view.activity.BaseActivity
import com.luxpmsoft.luxaipoc.view.activity.login.SignInActivity

class VerifyEmailActivity : BaseActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding

    var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Disable dark mode
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        MyUtils.setStatusBarTransparentFlagBlack(this)

        init()
        setListeners()
    }

    private fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            email = bundle.getString("email").toString()
        }
        binding.textSubtitle.text =
            getString(R.string.check_email_screen_subtitle, MyUtils.createEmailMask(email))
    }

    private fun setListeners() {
        binding.buttonResendLink.setOnClickListener { sendEmail() }
        binding.buttonBackToLogin.setOnClickListener { startSignInActivity() }
        binding.textNeedHelp.setOnClickListener { openMailApp() }
    }

    private fun sendEmail() {
        val emailVerification = EmailVerificationRequest()
        emailVerification.email = email
        APIOpenAirUtils.emailVerification(emailVerification, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val result = result as EmailVerificationResponse
                Toast.makeText(
                    this@VerifyEmailActivity,
                    result.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(error: Any?) {
                MyUtils.toastError(this@VerifyEmailActivity, error as ErrorModel)
            }
        })
    }

    private fun startSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}
