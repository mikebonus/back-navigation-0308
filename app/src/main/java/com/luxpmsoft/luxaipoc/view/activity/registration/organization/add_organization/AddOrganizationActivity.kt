package com.luxpmsoft.luxaipoc.view.activity.registration.organization.add_organization

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.databinding.ActivityAddOrganizationBinding
import com.luxpmsoft.luxaipoc.utils.*
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.SubscriptionManager

class AddOrganizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddOrganizationBinding

    private var isOrganizationNameFiled: Boolean = false
    private var isLogoUploaded: Boolean = false
    private var isFirstPermissionRequest = true
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var logo: ActivityResultLauncher<String>? = null
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        binding = ActivityAddOrganizationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setListeners()
        preparePermissions()
        setLogoContent()
        getOrganizationName()
    }

    private fun setListeners() {
        binding.topBar.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.editTextOrganizationName.doOnTextChanged { text, _, _, _ ->
            onOrganizationNameChange(text.toString().trim())
        }
        binding.buttonChooseFile.setOnClickListener {
            uploadLogo()
        }
        binding.viewUploadLogo.imageClose.setOnClickListener {
            binding.groupUploadLogo.isVisible = false
            isLogoUploaded = false
            setContinueButtonState()
        }
        binding.imageClear.setOnClickListener {
            binding.editTextOrganizationName.setText("")
        }
        binding.buttonContinue.setOnClickListener {
            SubscriptionManager().getSubType(
                SubscriptionType.Organization.type,
                binding.editTextOrganizationName.text.toString().trim(),
                this,
                binding.flProgress,
                AccountType.Organization.type,
                binding.editTextOrganizationName.text.toString().trim(),
            )
        }
    }

    private fun onOrganizationNameChange(name: String) {
        if (name.isNotEmpty()) {
            if (name.length < MIN_NAME_LENGTH) {
                setOrganizationErrorState()
            } else {
                setOrganizationDefaultState()
            }
            isOrganizationNameFiled = true
        } else {
            isOrganizationNameFiled = false
            setOrganizationDefaultState()
        }
        setContinueButtonState()
    }

    private fun setOrganizationErrorState() {
        binding.editTextOrganizationName.background = ContextCompat.getDrawable(
            this,
            R.drawable.bg_edit_text_error
        )
        binding.textNameError.isVisible = true
        binding.imageClear.isVisible = true
    }

    private fun setOrganizationDefaultState() {
        binding.editTextOrganizationName.background = ContextCompat.getDrawable(
            this,
            R.drawable.bg_edit_text_onboarding
        )
        binding.textNameError.isVisible = false
        binding.imageClear.isVisible = false
    }

    private fun setContinueButtonState() {
        if (isLogoUploaded && isOrganizationNameFiled) {
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

    private fun uploadLogo() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            logo?.launch("image/*")
        } else {
            if (isFirstPermissionRequest) {
                isFirstPermissionRequest = false
                requestPermission()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.common_media_permission), Toast.LENGTH_SHORT
                ).show()
                startPermissionSettings()
            }
        }
    }

    private fun preparePermissions() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (ContextCompat.checkSelfPermission(
                        this, android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    logo?.launch("image/*")
                }
            }
    }

    private fun requestPermission() {
        val isReadPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val permissionRequest = mutableListOf<String>()
        if (!isReadPermission) {
            permissionRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher?.launch(permissionRequest.toTypedArray())
        }
    }

    private fun setLogoContent() {
        logo = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it == null) {
                binding.groupUploadLogo.isVisible = false
            } else {
                uri = it
                val logoSize = getLogoSize()
                if ((logoSize ?: ERROR_LOGO_SIZE) > MAX_LOGO_SIZE) {
                    binding.groupUploadLogo.isVisible = false
                    Toast.makeText(
                        this,
                        getString(R.string.add_organization_screen_size_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.groupUploadLogo.isVisible = true
                    binding.viewUploadLogo.logo.setImageURI(it)
                    binding.viewUploadLogo.textFileSize.text =
                        getString(
                            R.string.common_file_size_mb,
                            logoSize?.convertByteToMB().toString()
                        )
                    val logoName = getFileName(uri ?: Uri.EMPTY)
                    binding.viewUploadLogo.textFileName.text = logoName
                    isLogoUploaded = true
                    setContinueButtonState()
                }
            }
        }
    }

    private fun getLogoSize(): Long? {
        val fileDescriptor = applicationContext.contentResolver.openAssetFileDescriptor(
            uri ?: Uri.EMPTY, "r"
        )
        val logoSize = fileDescriptor?.length
        fileDescriptor?.close()
        return logoSize
    }

    private fun getOrganizationName() {
        val bundle = intent.extras
        val organizationName = bundle?.getString(ORGANIZATION_NAME)
        if (organizationName != null) {
            binding.editTextOrganizationName.setText(organizationName)
        }
    }

    private fun startPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    companion object {
        private const val MAX_LOGO_SIZE = 99999999L
        private const val ERROR_LOGO_SIZE = 100000000L
        private const val MIN_NAME_LENGTH = 3
        const val ORGANIZATION_NAME = "organization_name"
    }
}
