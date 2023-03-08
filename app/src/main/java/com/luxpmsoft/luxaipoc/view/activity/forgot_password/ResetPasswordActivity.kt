package com.luxpmsoft.luxaipoc.view.activity.forgot_password

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.databinding.FragmentResetPasswordBinding
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.register.ResetPasswordRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.PasswordValidationPattern
import com.luxpmsoft.luxaipoc.utils.openMailApp
import com.luxpmsoft.luxaipoc.view.activity.login.SignInActivity
import kotlinx.android.synthetic.main.fragment_reset_password.*

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: FragmentResetPasswordBinding

    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Disable dark mode
        MyUtils.setStatusBarTransparentFlagBlack(this)
        binding = FragmentResetPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
        listener()
        binding.editTextPassword.doOnTextChanged { text, _, _, _ ->
            onPasswordChanged(
                text.toString().trim()
            )
        }
    }

    fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            token = bundle.getString("token").toString()
        }
    }

    fun listener() {
        binding.buttonConfirmPassword.setOnClickListener {
            val reset = ResetPasswordRequest()
            reset.newPassword = edit_text_password.text.toString()
            APIOpenAirUtils.resetPassword(token, reset, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Reset password Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    onResetPasswordSuccess()
                }

                override fun onError(error: Any?) {
                    MyUtils.toastError(this@ResetPasswordActivity, error as ErrorModel)
                }
            })
        }
        binding.buttonBackToLogin.setOnClickListener {
            startSignInActivity()
        }
        binding.topBar.buttonBack.setOnClickListener {
            startSignInActivity()
        }
        binding.textNeedHelp.setOnClickListener { openMailApp() }
        binding.textNeedHelpDown.setOnClickListener { openMailApp() }
    }

    private fun startSignInActivity() {
        val intent = Intent(this@ResetPasswordActivity, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onPasswordChanged(password: String) {
        if (password.isNotEmpty()) {
            setPasswordState(password)
        } else {
            setPasswordDefaultState()
        }
    }

    private fun setPasswordState(password: String) {
        binding.textInputLayoutPassword.isStartIconVisible = false
        val isAtLeastOneDigitPatterCorrect = checkPasswordPattern(
            password,
            PasswordValidationPattern.AtLeastOneDigit.pattern,
            binding.imageDotDigit
        )
        val isSpecialCharacterPatternCorrect = checkPasswordPattern(
            password,
            PasswordValidationPattern.SpecialCharacter.pattern,
            binding.imageDotSpecialCharacters
        )
        val isMinEightCharacterPatternCorrect = checkPasswordPattern(
            password,
            PasswordValidationPattern.MinEightCharacter.pattern,
            binding.imageDotCharacters
        )
        if (isAtLeastOneDigitPatterCorrect && isMinEightCharacterPatternCorrect && isSpecialCharacterPatternCorrect) {
            binding.buttonConfirmPassword.isEnabled = true
            binding.buttonConfirmPassword.background = ContextCompat.getDrawable(
                this,
                R.drawable.bg_purple_8
            )
        } else {
            binding.buttonConfirmPassword.isEnabled = false
            binding.buttonConfirmPassword.background = ContextCompat.getDrawable(
                this,
                R.drawable.bg_purple_8_disabled
            )
        }
    }

    private fun checkPasswordPattern(
        password: String,
        pattern: String,
        dotView: ImageView
    ): Boolean {
        val isPatterCorrect = MyUtils.isPasswordValid(password, pattern)
        setDotState(dotView, isPatterCorrect)
        return isPatterCorrect
    }

    private fun setDotState(dotView: ImageView, isPatterCorrect: Boolean) {
        if (isPatterCorrect) {
            dotView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_dot_green
                )
            )
        } else {
            dotView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_dot_white
                )
            )
        }
    }

    private fun setPasswordDefaultState() {
        binding.buttonConfirmPassword.background = ContextCompat.getDrawable(
            this,
            R.drawable.bg_purple_8_disabled
        )
        binding.imageDotCharacters.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_dot_white
            )
        )
        binding.imageDotDigit.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_dot_white
            )
        )
        binding.imageDotSpecialCharacters.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_dot_white
            )
        )
        binding.buttonConfirmPassword.isEnabled = false
        binding.textInputLayoutPassword.isStartIconVisible = true
    }

    private fun onResetPasswordSuccess() {
        binding.groupReset.isVisible = false
        binding.groupLogin.isVisible = true
        binding.textSubtitle.text = getString(R.string.password_confirmed_screen_subtitle)
    }
}