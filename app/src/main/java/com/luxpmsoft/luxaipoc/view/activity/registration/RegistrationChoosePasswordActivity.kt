package com.luxpmsoft.luxaipoc.view.activity.registration

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.databinding.ActivityRegistrationChoosePasswordBinding
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.register.RegisterRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.PasswordValidationPattern

class RegistrationChoosePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationChoosePasswordBinding

    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        binding = ActivityRegistrationChoosePasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setListeners()
        getAccountDetails()
    }

    private fun setListeners() {
        binding.editTextPassword.doOnTextChanged { text, _, _, _ ->
            onPasswordChanged(
                text.toString().trim()
            )
        }
        binding.buttonContinue.setOnClickListener {
            MyUtils.showProgress(this@RegistrationChoosePasswordActivity, binding.flProgress)
            val register = RegisterRequest()
            register.first_name = firstName
            register.last_name = lastName
            register.email = email
            register.phone = generateRandomNumber()
            register.password = binding.editTextPassword.text.toString()
            APIOpenAirUtils.register(register, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(
                        this@RegistrationChoosePasswordActivity,
                        binding.flProgress
                    )
                    startCheckEmailScreen()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(
                        this@RegistrationChoosePasswordActivity,
                        binding.flProgress
                    )
                    MyUtils.toastError(this@RegistrationChoosePasswordActivity, error as ErrorModel)
                }
            })
        }
    }

    private fun startCheckEmailScreen() {
        val intent =
            Intent(this@RegistrationChoosePasswordActivity, VerifyEmailActivity::class.java)
        intent.putExtra(SignUpActivity.EMAIL, email)
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
        val isAtLeastOneUppercaseCorrect = checkPasswordPattern(
            password,
            PasswordValidationPattern.AtLeastOneUppercase.pattern,
            binding.imageDotUppercase
        )
        if (isAtLeastOneDigitPatterCorrect && isMinEightCharacterPatternCorrect
            && isSpecialCharacterPatternCorrect && isAtLeastOneUppercaseCorrect
        ) {
            binding.buttonContinue.isEnabled = true
            binding.buttonContinue.background = ContextCompat.getDrawable(
                this,
                R.drawable.bg_purple_8
            )
        } else {
            binding.buttonContinue.isEnabled = false
            binding.buttonContinue.background = ContextCompat.getDrawable(
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

    private fun setDotState(dotView: ImageView, isPatterCorrect: Boolean): Boolean {
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
        return isPatterCorrect
    }

    private fun setPasswordDefaultState() {
        binding.buttonContinue.background = ContextCompat.getDrawable(
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
        binding.buttonContinue.isEnabled = false
        binding.textInputLayoutPassword.isStartIconVisible = true
    }

    private fun generateRandomNumber(): String {
        return (111111111..999999999).random().toString()
    }

    private fun getAccountDetails() {
        val bundle = intent.extras
        firstName = bundle?.getString(SignUpActivity.FIRST_NAME)
        lastName = bundle?.getString(SignUpActivity.LAST_NAME)
        email = bundle?.getString(SignUpActivity.EMAIL)
    }
}