package com.luxpmsoft.luxaipoc.view.activity.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.ConstantAPI
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.databinding.ActivitySignIn1Binding
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.GoogleResponse
import com.luxpmsoft.luxaipoc.model.ModelResponse
import com.luxpmsoft.luxaipoc.model.home.SubscriptionResponse
import com.luxpmsoft.luxaipoc.model.login.LoginRequest
import com.luxpmsoft.luxaipoc.model.login.LoginResponse
import com.luxpmsoft.luxaipoc.model.login.LoginSNSRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeDetailResponse
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.HomeActivity
import com.luxpmsoft.luxaipoc.view.activity.HomeOrganizationActivity
import com.luxpmsoft.luxaipoc.view.activity.PricingPlanActivity
import com.luxpmsoft.luxaipoc.view.activity.forgot_password.ForgotPasswordActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.SignUpActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.VerifyEmailActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.SelectAccountTypeActivity
import com.luxpmsoft.luxaipoc.view.activity.welcome.WelcomeActivity
import com.luxpmsoft.luxaipoc.widget.FacebookLogin
import com.luxpmsoft.luxaipoc.widget.SignInApple
import com.luxpmsoft.luxaipoc.widget.SignInGoogle
import kotlinx.android.synthetic.main.activity_sign_in1.*

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignIn1Binding

    // Login with Social
    private var loginGoogle: SignInGoogle? = null
    private var signInApple: SignInApple? = null
    private var loginFacebook: FacebookLogin? = null
    var doubleBackToExitPressedOnce = false
    private var isEmailCorrect = false
    private var isPasswordCorrect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivitySignIn1Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        MyUtils.setStatusBarTransparentFlagBlack(this)        // back-navigation issue
        binding.topBar.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val bundle = intent.extras
        if (bundle != null) {
            val email = bundle.get("email").toString()
            val password = bundle.get("password").toString()
            login(email, password)
        }

        loginGoogle = SignInGoogle(this)
        signInApple = SignInApple(this)
        loginFacebook = FacebookLogin(this)

        //Get Firebase auth instance
        binding.buttonLogIn.setOnClickListener {
            if (validation()) {
                login(edit_text_email.text?.trim().toString(), edit_text_password.text.toString())
            }
        }
        binding.textSignUp.setOnClickListener {
            doubleClick()
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.textForgotPassword.setOnClickListener {
            doubleClick()
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        binding.logos.imageGoogleLogo.setOnClickListener {
            if (!doubleBackToExitPressedOnce) {
                doubleBackToExitPressedOnce = true
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
                                        this@SignInActivity,
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
        }
        binding.topBar.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.logos.imageFacebookLogo.setOnClickListener {
            if (!doubleBackToExitPressedOnce) {
                doubleBackToExitPressedOnce = true
                loginFaceBook()
            }
        }

        binding.editTextEmail.doOnTextChanged { text, _, _, _ ->
            onEmailChanged(
                text.toString().trim()
            )
        }
        binding.editTextPassword.doOnTextChanged { text, _, _, _ ->
            onPasswordChanged(text.toString().trim())
        }
    }

    private fun onEmailChanged(email: String) {
        if (email.isNotEmpty()) {
            if (!MyUtils.isEmailValid(email)) {
                setEmailErrorState()
            } else {
                setEmailCheckState()
            }
        } else {
            setEmailDefaultState()
        }
    }

    private fun onPasswordChanged(password: String) {
        if (password.isNotEmpty()) {
            if (!MyUtils.Password_Validation(password)) {
                setPasswordErrorState()
            } else {
                setPasswordCheckState()
            }
        } else {
            setPasswordDefaultState()
        }
    }

    private fun setEmailErrorState() {
        isEmailCorrect = false
        binding.textIncorrectCredentials.isVisible = true
        binding.textInputLayoutEmail.isStartIconVisible = false
        setButtonSet()
    }

    private fun setEmailCheckState() {
        isEmailCorrect = true
        binding.textIncorrectCredentials.isVisible = false
        binding.textInputLayoutEmail.isStartIconVisible = false
        setButtonSet()
    }

    private fun setEmailDefaultState() {
        isEmailCorrect = false
        binding.textIncorrectCredentials.isVisible = false
        binding.textInputLayoutEmail.isStartIconVisible = true
        setButtonSet()
    }

    private fun setPasswordErrorState() {
        isPasswordCorrect = false
        binding.textIncorrectCredentials.isVisible = true
        binding.textInputLayoutPassword.isStartIconVisible = false
        setButtonSet()
    }

    private fun setPasswordCheckState() {
        isPasswordCorrect = true
        binding.textIncorrectCredentials.isVisible = false
        binding.textInputLayoutPassword.isStartIconVisible = false
        setButtonSet()
    }

    private fun setPasswordDefaultState() {
        isPasswordCorrect = false
        binding.textIncorrectCredentials.isVisible = false
        binding.textInputLayoutPassword.isStartIconVisible = true
        setButtonSet()
    }

    private fun setButtonSet() {
        if (isEmailCorrect && isPasswordCorrect) {
            binding.buttonLogIn.isEnabled = true
            binding.buttonLogIn.background = ContextCompat.getDrawable(
                this,
                R.drawable.bg_purple_8
            )
        } else {
            binding.buttonLogIn.isEnabled = false
            binding.buttonLogIn.background = ContextCompat.getDrawable(
                this,
                R.drawable.bg_purple_8_disabled
            )
        }
    }

    private fun doubleClick() {
        if (doubleBackToExitPressedOnce) {
            return
        }

        doubleBackToExitPressedOnce = true

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 1100)
    }

    fun login(email: String, password: String) {
        MyUtils.showProgress(this, flProgress)
        val login = LoginRequest()
        login.email = email
        login.password = password
        APIOpenAirUtils.login(login, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val detail = result as LoginResponse
                detail?.body?.also {
                    if (it.id != null) {
                        ConstantAPI.USER_ID = it.id!!
                        (application as LidarApp).prefManager?.setUserId(
                            it.id!!
                        )
                    }

                    (application as LidarApp).prefManager?.setToken(
                        it.threeD?.token!!
                    )

                    it.subscriptionName?.also {
                        (application as LidarApp).prefManager?.setSubscriptionName(
                            it
                        )
                    }

                    it.subscriptionType?.also {
                        (application as LidarApp).prefManager?.setUserType(
                            it
                        )
                    }

                    it.profileImageKey?.also {
                        (application as LidarApp).prefManager?.setProfileImageKey(
                            it
                        )
                    }

                    it.hasSub?.also {
                        (application as LidarApp).prefManager?.setHasSub(
                            it
                        )
                    }

                    it.organizationId?.also {
                        (application as LidarApp).prefManager?.setOrganizationId(
                            it
                        )
                    }

                    it.organizationRole?.also {
                        (application as LidarApp).prefManager?.setOrganizationRole(
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
//                            getSubType()
                    startSelectAccountTypeActivity()
                }
                MyUtils.hideProgress(this@SignInActivity, flProgress)
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@SignInActivity, flProgress)
                val error = error as ErrorModel
                if (error.message?.contains("Error: Please verify your email to login")!!) {
                    val intent = Intent(this@SignInActivity, VerifyEmailActivity::class.java)
                    intent.putExtra("email", edit_text_email.text.toString())
                    startActivity(intent)
                } else {
                    MyUtils.toastError(this@SignInActivity, error)
                    Log.e("STAS", error.message ?: "a")
                    binding.textIncorrectCredentials.isVisible = true
                }
            }
        })
    }

    fun loginSNS(login: LoginSNSRequest) {
        MyUtils.showProgress(this, flProgress)
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
//                    getSubType()
                    startSelectAccountTypeActivity()
                }
                MyUtils.hideProgress(this@SignInActivity, flProgress)
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@SignInActivity, flProgress)
                val error = error as ErrorModel
                if (error.message?.contains("Error: Please verify your email to login")!!) {
                    val intent = Intent(this@SignInActivity, VerifyEmailActivity::class.java)
                    intent.putExtra("email", edit_text_email.text.toString())
                    startActivity(intent)
                } else {
                    MyUtils.toastError(this@SignInActivity, error)
                }
            }
        })
    }

    fun startPricingPlan() {
        val intent = Intent(this@SignInActivity, PricingPlanActivity::class.java)
        startActivity(intent)
    }

    // Login facebook
    private fun loginFaceBook() {
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
                Toast.makeText(this@SignInActivity, "Login Cancel", Toast.LENGTH_LONG).show()
            }

            override fun onError(response: ModelResponse?) {
                doubleBackToExitPressedOnce = false
                Toast.makeText(this@SignInActivity, "Login Error", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun getSubType() {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getSubscriptionType(object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as SubscriptionTypeResponse
                data?.body?.rows?.also {
                    for (sub in it) {
                        if (sub.type?.contains("Individual")!!) {
                            getSubDetailType(sub.subscriptionTypeID, "Individual")
                        }
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@SignInActivity, flProgress)
                MyUtils.toastError(this@SignInActivity, error as ErrorModel)
            }
        })
    }

    fun getSubDetailType(subscriptionTypeID: String?, type: String?) {
        APIOpenAirUtils.getSubscription(subscriptionTypeID, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as SubscriptionTypeDetailResponse
                data?.body?.also {
                    for (subType in it) {
                        if (subType.subscriptionName == "Premium") {
                            val subscription = SubscriptionRequest()
                            subscription.subscriptionID = subType.subscriptionID
                            addSubscription(subscription, type)
                        }
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@SignInActivity, flProgress)
                MyUtils.toastError(this@SignInActivity, error as ErrorModel)
            }
        })
    }

    fun addSubscription(subscriptionRequest: SubscriptionRequest, type: String?) {
        APIOpenAirUtils.addSubscription((application as LidarApp).prefManager!!.getToken(),
            subscriptionRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@SignInActivity, flProgress)
                    val data = result as SubscriptionResponse
                    (application as LidarApp).prefManager!!.setHasSub(
                        "true"
                    )
                    startHome(type!!.toLowerCase())
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@SignInActivity, flProgress)
                    MyUtils.toastError(this@SignInActivity, error as ErrorModel)
                }
            })
    }

    fun validation(): Boolean {
        var isValidate = true
        if (!MyUtils.isEmailValid(edit_text_email.text?.trim().toString())) {
            Toast.makeText(this, resources.getString(R.string.email_error), Toast.LENGTH_SHORT)
                .show()
            isValidate = false
        }
//        else {
//            if(!MyUtils.Password_Validation(edtPassword.text.toString())) {
//                Toast.makeText(this, resources.getString(R.string.password_error), Toast.LENGTH_SHORT).show()
//                isValidate = false
//            }
//        }
        return isValidate
    }

    fun startHome(type: String) {
        val intent = if (type == "individual") {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, HomeOrganizationActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    fun startSelectAccountTypeActivity() {
        val intent = Intent(this@SignInActivity, SelectAccountTypeActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99) {
            loginGoogle!!.onActivityResult(requestCode, resultCode, data)
        }
        loginFacebook?.onActivityResult(requestCode, resultCode, data);
    }
}