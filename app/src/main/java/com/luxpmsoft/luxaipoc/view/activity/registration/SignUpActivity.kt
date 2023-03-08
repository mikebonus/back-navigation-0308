package com.luxpmsoft.luxaipoc.view.activity.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.ConstantAPI
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.databinding.ActivitySignUpBinding
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.GoogleResponse
import com.luxpmsoft.luxaipoc.model.ModelResponse
import com.luxpmsoft.luxaipoc.model.login.LoginResponse
import com.luxpmsoft.luxaipoc.model.login.LoginSNSRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.AlmostThereActivity
import com.luxpmsoft.luxaipoc.view.activity.HomeActivity
import com.luxpmsoft.luxaipoc.view.activity.HomeOrganizationActivity
import com.luxpmsoft.luxaipoc.view.activity.login.SignInActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.policy.privacy_policy.PrivacyPolicyActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.policy.terms_of_service.TermsOfServiceActivity
import com.luxpmsoft.luxaipoc.widget.FacebookLogin
import com.luxpmsoft.luxaipoc.widget.SignInGoogle
import kotlinx.android.synthetic.main.activity_sign_in1.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private var isFirstNameEmpty = true
    private var isLastNameEmpty = true
    private var isEmailValid = false
    private var doubleBackToExitPressedOnce = false
    private var loginGoogle: SignInGoogle? = null
    private var loginFacebook: FacebookLogin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        MyUtils.setStatusBarTransparentFlagBlack(this)

        loginGoogle = SignInGoogle(this)
        loginFacebook = FacebookLogin(this)
        setListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99) {
            loginGoogle!!.onActivityResult(requestCode, resultCode, data)
        }
        loginFacebook?.onActivityResult(requestCode, resultCode, data);
    }

    private fun setListeners() {
        binding.buttonContinue.setOnClickListener {
            val intent = Intent(this@SignUpActivity, RegistrationChoosePasswordActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(FIRST_NAME, binding.editTextFirstName.text.toString())
            intent.putExtra(LAST_NAME, binding.editTextLastName.text.toString())
            intent.putExtra(EMAIL, binding.editTextEmail.text.toString())
            startActivity(intent)
            finish()
        }
        binding.textSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.topBar.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.editTextFirstName.doOnTextChanged { text, _, _, _ ->
            onFirstNameChanged(
                text.toString().trim()
            )
        }
        binding.editTextLastName.doOnTextChanged { text, _, _, _ ->
            onLastNameChanged(
                text.toString().trim()
            )
        }
        binding.editTextEmail.doOnTextChanged { text, _, _, _ ->
            onEmailChanged(
                text.toString().trim()
            )
        }
        binding.logos.imageGoogleLogo.setOnClickListener {
            if (!doubleBackToExitPressedOnce) {
                doubleBackToExitPressedOnce = true
                loginGoogle()
            }
        }
        binding.logos.imageFacebookLogo.setOnClickListener {
            if (!doubleBackToExitPressedOnce) {
                doubleBackToExitPressedOnce = true
                loginFacebook()
            }
        }
        binding.viewPrivacyPolicy.textTerms.setOnClickListener {
            startTermsOfServiceActivity()
        }

        binding.viewPrivacyPolicy.textPolicy.setOnClickListener {
            startPrivacyPolicyActivity()
        }
    }

    private fun onFirstNameChanged(firstName: String) {
        isFirstNameEmpty = firstName.isEmpty()
        setContinueButtonState()
    }

    private fun onLastNameChanged(lastName: String) {
        isLastNameEmpty = lastName.isEmpty()
        setContinueButtonState()
    }

    private fun onEmailChanged(email: String) {
        isEmailValid = MyUtils.isEmailValid(email)
        setContinueButtonState()
    }

    private fun setContinueButtonState() {
        if (isEmailValid && !isFirstNameEmpty && !isLastNameEmpty) {
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

    private fun loginGoogle() {
        loginGoogle!!.signOutGoogle()
        loginGoogle!!.loginWithGoogle(object : SignInGoogle.Listener.responseLogin {
            override fun onSuccess(account: GoogleResponse?) {
                doubleBackToExitPressedOnce = false
                if (account?.idToken != null) {
                    val login = LoginSNSRequest()
                    login.first_name = account?.personGivenName
                    login.last_name = account?.personFamilyName
                    login.email = account?.personEmail
                    login.snsType = "google"
                    login.snsToken = account?.personId
                    login.profileImage = account?.personPhoto?.path
                    loginSNS(login)
                }
            }

            override fun onError(error: String?) {
                doubleBackToExitPressedOnce = false
                try {
                    runOnUiThread {
                        if (error == null) {
                            Toast.makeText(
                                this@SignUpActivity,
                                "SignInGoogle onError",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.message
                }
            }
        })
    }

    fun loginFacebook() {
        loginFacebook?.logoutFacebook()
        loginFacebook?.loginFacebook(object : FacebookLogin.Listener.responseListener {
            override fun onSuccess(response: ModelResponse?) {
                doubleBackToExitPressedOnce = false
                if (response != null && response.accessToken != null) {
                    val login = LoginSNSRequest()
                    login.first_name = response?.personGivenName
                    login.last_name = response?.personFamilyName
                    login.email = response?.personEmail
                    login.snsType = "facebook"
                    login.snsToken = response?.userId
                    login.profileImage = response?.personPhoto
                    loginSNS(login)
                }
            }

            override fun onCancel(response: ModelResponse?) {
                doubleBackToExitPressedOnce = false
                Toast.makeText(this@SignUpActivity, "Login Cancel", Toast.LENGTH_LONG).show()
            }

            override fun onError(response: ModelResponse?) {
                doubleBackToExitPressedOnce = false
                Toast.makeText(this@SignUpActivity, "Login Error", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun loginSNS(login: LoginSNSRequest) {
        MyUtils.showProgress(this, binding.flProgress)
        APIOpenAirUtils.loginSNS(login, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val detail = result as LoginResponse
                detail?.body?.also {
                    if (it.id != null) {
                        ConstantAPI.USER_ID = it.id!!
                        (application as LidarApp).prefManager!!.setUserId(
                            it.id!!
                        )
                    }

                    (application as LidarApp).prefManager!!.setToken(
                        it.threeD?.token!!
                    )

                    it.subscriptionName?.also {
                        (application as LidarApp).prefManager!!.setSubscriptionName(
                            it
                        )
                    }

                    it.subscriptionType?.also {
                        (application as LidarApp).prefManager!!.setUserType(
                            it
                        )
                    }

                    it.profileImageKey?.also {
                        (application as LidarApp).prefManager?.setProfileImageKey(
                            it
                        )
                    }

                    it.hasSub?.also {
                        (application as LidarApp).prefManager!!.setHasSub(
                            it
                        )
                    }

                    it.hasSub?.also {
                        (application as LidarApp).prefManager!!.setHasSub(
                            it
                        )
                    }

                    it.full_name?.let {
                        (application as LidarApp).prefManager?.setFullName(
                            it
                        )
                    }
                }

                val user = Gson().toJson(detail)
                (application as LidarApp).prefManager!!.setUser(user)

                if (detail?.body?.hasSub == "true") {
                    startHome(detail?.body?.subscriptionType!!)
                } else {
                    startAlmost()
                }
                MyUtils.hideProgress(this@SignUpActivity, binding.flProgress)
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@SignUpActivity, binding.flProgress)
                val error = error as ErrorModel
                if (error.message?.contains("Error: Please verify your email to login")!!) {
                    val intent = Intent(this@SignUpActivity, VerifyEmailActivity::class.java)
                    intent.putExtra("email", edit_text_email.text.toString())
                    startActivity(intent)
                } else {
                    MyUtils.toastError(this@SignUpActivity, error)
                }
            }
        })
    }

    fun startHome(type: String) {
        if (type == "individual") {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, HomeOrganizationActivity::class.java)
            startActivity(intent)
        }

        finish()
    }

    fun startAlmost() {
        val intent = Intent(this@SignUpActivity, AlmostThereActivity::class.java)
        startActivity(intent)
    }

    private fun startTermsOfServiceActivity() {
        val intent = Intent(this@SignUpActivity, TermsOfServiceActivity::class.java)
        startActivity(intent)
    }

    private fun startPrivacyPolicyActivity() {
        val intent = Intent(this@SignUpActivity, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last name"
        const val EMAIL = "email"
    }
}