package com.luxpmsoft.luxaipoc.view.activity.registration.organization

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luxpmsoft.luxaipoc.databinding.ActivitySelectAccountTypeBinding
import com.luxpmsoft.luxaipoc.utils.*
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.add_organization.AddOrganizationActivity

class SelectAccountTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectAccountTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectAccountTypeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        MyUtils.setStatusBarTransparentFlagBlack(this)

        setListeners()
    }

    private fun setListeners() {
        binding.viewIndividualAccount.buttonContinue.setOnClickListener {
            SubscriptionManager().getSubType(
                SubscriptionType.Individual.type,
                null,
                this,
                binding.flProgress,
                AccountType.Individual.type,
                null
            )
        }
        binding.viewBusinessAccount.buttonAddOrganization.setOnClickListener {
            startAddOrganizationActivity()
        }
        binding.viewBusinessAccount.buttonJoinOrganization.setOnClickListener {
            startJoinOrganizationActivity()
        }
    }

    private fun startAddOrganizationActivity() {
        val intent = Intent(this, AddOrganizationActivity::class.java)
        startActivity(intent)
    }

    private fun startJoinOrganizationActivity() {
        val intent = Intent(this, JoinOrganizationActivity::class.java)
        startActivity(intent)
    }
}
