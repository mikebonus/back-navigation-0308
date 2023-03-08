package com.luxpmsoft.luxaipoc.view.activity.forgot_password

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.databinding.ActivityForgotPasswordBinding
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.register.ForgetPasswordRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.openMailApp
import com.luxpmsoft.luxaipoc.view.activity.login.SignInActivity

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Disable dark mode
        MyUtils.setStatusBarTransparentFlagBlack(this)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setListeners()
    }

    private fun setListeners() {
        binding.buttonResetPassword.setOnClickListener {
            if (binding.buttonResetPassword.text == getString(R.string.forgot_password_screen_reset_password)) {
                    val forget = ForgetPasswordRequest()
                    forget.email = binding.editTextEmail.text?.trim().toString()
                    APIOpenAirUtils.forgetPassword(
                        (application as LidarApp).prefManager!!.getToken(),
                        forget,
                        object : APIInterface.onDelegate {
                            override fun onSuccess(result: Any?) {
                                onPasswordResetSuccess()
                            }

                            override fun onError(error: Any?) {
                                MyUtils.toastError(this@ForgotPasswordActivity, error as ErrorModel)
                            }
                        })
            } else {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.editTextEmail.doOnTextChanged { text, _, _, _ ->
            onEmailChanged(
                text.toString().trim()
            )
        }

        binding.topBar.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.textNeedHelp.setOnClickListener { openMailApp() }
    }

    private fun onEmailChanged(email: String) {
        if (MyUtils.isEmailValid(email)) {
            setEmailCheckState()
        } else {
            setEmailDefaultState()
        }
    }

    private fun setEmailCheckState() {
        binding.textInputLayoutEmail.isStartIconVisible = true
        binding.buttonResetPassword.isEnabled = true
        binding.buttonResetPassword.background = ContextCompat.getDrawable(
            this,
            R.drawable.bg_purple_8
        )
    }

    private fun setEmailDefaultState() {
        binding.textInputLayoutEmail.isStartIconVisible = true
        binding.buttonResetPassword.isEnabled = false
        binding.buttonResetPassword.background = ContextCompat.getDrawable(
            this,
            R.drawable.bg_purple_8_disabled
        )
    }

    private fun onPasswordResetSuccess() {
        binding.textTitle.text = getString(R.string.forgot_password_screen_check_your_email)
        binding.textSubtitle.isVisible = false
        binding.textSubtitleDone.isVisible = true
        binding.textSubtitleDone.text =
            getString(
                R.string.forgot_password_screen_we_sent_email,
                MyUtils.createEmailMask(binding.editTextEmail.text.toString())
            )
        binding.textCheckEmail.isVisible = true
        binding.textInputLayoutEmail.isVisible = false
        binding.buttonResetPassword.text = getString(R.string.forgot_password_screen_back_to_login)
    }
}