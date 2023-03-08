package com.luxpmsoft.luxaipoc.view.activity.registration.organization

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.databinding.ActivityJoinOrganizationBinding
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.organization.OrganizationByNameRequest
import com.luxpmsoft.luxaipoc.model.organization.OrganizationByNameResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.add_organization.AddOrganizationActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.add_organization.AddOrganizationActivity.Companion.ORGANIZATION_NAME

class JoinOrganizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinOrganizationBinding

    private var isOrganizationNameFiled: Boolean = false
    private var role: String? = null
    private var whiteColorList: ColorStateList? = null
    private var purpleColorList: ColorStateList? = null
    private var organizationName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        binding = ActivityJoinOrganizationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        getBundle()
        setColors()
        setListeners()
    }

    private fun getBundle() {
        val bundle = intent.extras
        organizationName = bundle?.getString(ORGANIZATION_NAME)
        if (organizationName != null) {
            binding.editTextOrganizationName.setText(organizationName)
        }
    }

    private fun setColors() {
        whiteColorList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_enabled)
            ), intArrayOf(
                getColor(R.color.white),
                getColor(R.color.white)
            )
        )
        purpleColorList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_enabled)
            ), intArrayOf(
                getColor(R.color.secondary_purple_600),
                getColor(R.color.secondary_purple_600)
            )
        )
    }

    private fun setListeners() {
        binding.topBar.buttonBack.setOnClickListener {
            startSelectAccountTypeActivity()
        }
        binding.editTextOrganizationName.doOnTextChanged { text, _, _, _ ->
            onOrganizationNameChange(text.toString().trim())
        }
        binding.radioButtonUser.setOnClickListener { onRadioButtonClicked(it) }
        binding.radioButtonAdmin.setOnClickListener { onRadioButtonClicked(it) }
        binding.buttonContinue.setOnClickListener {
            SubscriptionManager().organizationJoinExisting(
                binding.editTextOrganizationName.text.toString().trim(),
                this,
                binding.flProgress
            )
        }
        binding.viewDropDownMenu.textAddOrganization.setOnClickListener {
            startAddOrganizationActivity(binding.editTextOrganizationName.text.toString())
        }
        binding.viewDropDownMenu.textOrganizationName.setOnClickListener {
            if (binding.viewDropDownMenu.textOrganizationName.text != getString(R.string.join_organization_screen_no_business_found)) {
                binding.editTextOrganizationName.setText(binding.viewDropDownMenu.textOrganizationName.text)
                setDropDownMenuState(false)
            }
        }
    }

    private fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked
            when (view.getId()) {
                binding.radioButtonUser.id -> {
                    if (checked) {
                        role = binding.radioButtonUser.text.toString()
                        binding.radioButtonUser.buttonTintList = purpleColorList
                        binding.radioButtonAdmin.buttonTintList = whiteColorList
                        setContinueButtonState()
                    }
                }
                binding.radioButtonAdmin.id -> {
                    if (checked) {
                        role = binding.radioButtonUser.text.toString()
                        binding.radioButtonUser.buttonTintList = whiteColorList
                        binding.radioButtonAdmin.buttonTintList = purpleColorList
                        setContinueButtonState()
                    }
                }
            }
        }
    }

    private fun onOrganizationNameChange(name: String) {
        if (name.isNotEmpty()) {
            isOrganizationNameFiled = true
            getOrganizationByName(name)
            setDropDownMenuState(true)
        } else {
            isOrganizationNameFiled = false
            setDropDownMenuState(false)
        }
        setContinueButtonState()
    }

    private fun setDropDownMenuState(isDropDownMenuExpanded: Boolean) {
        if (isDropDownMenuExpanded) {
            binding.groupDropDownMenu.isVisible = true
            binding.textInputLayoutMessage.visibility = View.INVISIBLE
            binding.textRoleRequested.visibility = View.INVISIBLE
            binding.editTextOrganizationName.background = ContextCompat.getDrawable(
                this,
                R.drawable.bg_edit_text_top_rounded
            )
        } else {
            binding.groupDropDownMenu.isVisible = false
            binding.textInputLayoutMessage.visibility = View.VISIBLE
            binding.textRoleRequested.visibility = View.VISIBLE
            binding.editTextOrganizationName.background = ContextCompat.getDrawable(
                this,
                R.drawable.bg_edit_text_onboarding
            )
        }

    }

    private fun setContinueButtonState() {
        if (role != null && isOrganizationNameFiled) {
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

    private fun getOrganizationByName(organizationName: String) {
        val request = OrganizationByNameRequest()
        request.organization = organizationName
        APIOpenAirUtils.getOrganizationByName(request,
            (application as LidarApp).prefManager!!.getToken(),
            object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    val data = result as OrganizationByNameResponse
                    if (data.lists != null) {
                        binding.viewDropDownMenu.textOrganizationName.text = data.lists.name
                    } else {
                        binding.viewDropDownMenu.textOrganizationName.text =
                            getString(R.string.join_organization_screen_no_business_found)
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.toastError(this@JoinOrganizationActivity, error as ErrorModel)
                }
            })
    }

    private fun startAddOrganizationActivity(organizationName: String) {
        val intent = Intent(this, AddOrganizationActivity::class.java)
        intent.putExtra(ORGANIZATION_NAME, organizationName)
        startActivity(intent)
    }

    private fun startSelectAccountTypeActivity() {
        val intent = Intent(this, SelectAccountTypeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
