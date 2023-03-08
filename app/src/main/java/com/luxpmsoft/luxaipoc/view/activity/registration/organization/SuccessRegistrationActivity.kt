package com.luxpmsoft.luxaipoc.view.activity.registration.organization

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.databinding.ActivitySuccessRegistrationBinding
import com.luxpmsoft.luxaipoc.utils.DESTINATION
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.ORGANIZATION_NAME
import com.luxpmsoft.luxaipoc.utils.RegistrationFlow
import com.luxpmsoft.luxaipoc.view.activity.HomeActivity
import com.luxpmsoft.luxaipoc.view.activity.HomeOrganizationActivity

class SuccessRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuccessRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessRegistrationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        MyUtils.setStatusBarTransparentFlagBlack(this)

        setContent()
    }

    private fun setContent() {
        val bundle = intent.extras
        when (bundle?.getString(DESTINATION)) {
            RegistrationFlow.AddOrganization.destination -> setAddOrganizationContent(
                bundle?.getString(ORGANIZATION_NAME)
            )
            RegistrationFlow.JoinOrganization.destination -> setJoinOrganizationContent(
                bundle?.getString(ORGANIZATION_NAME)
            )
            RegistrationFlow.CreatedAccount.destination -> setCreatedAccountContent()
        }
    }

    private fun setAddOrganizationContent(organizationName: String?) {
        binding.textTitle.text =
            getString(R.string.success_registration_screen_add_organization_title)
        binding.textSubtitle.text =
            getString(
                R.string.success_registration_screen_add_organization_subtitle,
                organizationName
            )
        binding.buttonContinue.setOnClickListener {
            startHomeOrganizationActivity()
        }
    }

    private fun setJoinOrganizationContent(organizationName: String?) {
        binding.textTitle.text =
            getString(R.string.success_registration_screen_join_organization_title)
        binding.textSubtitle.text =
            getString(
                R.string.success_registration_screen_join_organization_subtitle,
                "organizationName"
            )
        binding.buttonContinue.setOnClickListener { startHomeActivity() }
    }

    private fun setCreatedAccountContent() {
        binding.textTitle.text =
            getString(R.string.success_registration_screen_created_account_title)
        binding.textSubtitle.text =
            getString(R.string.success_registration_screen_created_account_subtitle)
        binding.buttonContinue.setOnClickListener { startHomeActivity() }
    }

    private fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun startHomeOrganizationActivity() {
        val intent = Intent(this, HomeOrganizationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}
