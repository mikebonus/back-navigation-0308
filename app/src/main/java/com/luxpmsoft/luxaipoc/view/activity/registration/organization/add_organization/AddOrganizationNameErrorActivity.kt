package com.luxpmsoft.luxaipoc.view.activity.registration.organization.add_organization

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.databinding.ActivityAddOrganizationNameErrorBinding
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.JoinOrganizationActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.add_organization.AddOrganizationActivity.Companion.ORGANIZATION_NAME

class AddOrganizationNameErrorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddOrganizationNameErrorBinding
    private var organizationName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        binding = ActivityAddOrganizationNameErrorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        getBundle()
        setContent()
        setListeners()
    }

    private fun getBundle() {
        val bundle = intent.extras
        organizationName = bundle?.getString(ORGANIZATION_NAME)
    }

    private fun setContent() {
        binding.viewOrganizationNameError.textTitle.text =
            getString(R.string.add_organization_screen_name_error_title, organizationName)
        binding.viewOrganizationNameError.textSubtitle.text =
            getString(R.string.add_organization_screen_name_error_subtitle, organizationName)
    }

    private fun setListeners() {
        binding.topBar.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.buttonTryAnotherName.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.buttonJoinOrganization.setOnClickListener {
            startJoinOrganizationActivity()
        }
    }

    private fun startJoinOrganizationActivity() {
        val intent = Intent(this, JoinOrganizationActivity::class.java)
        intent.putExtra(ORGANIZATION_NAME, organizationName)
        startActivity(intent)
        finish()
    }
}
