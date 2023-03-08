package com.luxpmsoft.luxaipoc.view.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import com.google.ar.core.Session
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.register.ResetPasswordResponse
import com.luxpmsoft.luxaipoc.model.user.ChangePasswordRequest
import com.luxpmsoft.luxaipoc.model.user.UpdateUserRequest
import com.luxpmsoft.luxaipoc.model.user.UserDetailResponse
import com.luxpmsoft.luxaipoc.model.user.UserImageResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.welcome.WelcomeActivity
import com.luxpmsoft.luxaipoc.widget.CaptureImage
import kotlinx.android.synthetic.main.activity_user_profile.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.util.*

class UserProfileActivity: BaseActivity(), CaptureImage.onCaptureImage  {
    var captureImage: CaptureImage? = null
    var detail: UserDetailResponse? = null
    private var session: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Disable dark mode
        setContentView(R.layout.activity_user_profile)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        if ((application as LidarApp).prefManager?.getLocale().equals("en")) {
            setLocale(this@UserProfileActivity, "en")
            tvChooseLocale.text = resources.getString(R.string.english)
        } else if ((application as LidarApp).prefManager?.getLocale().equals("ko")) {
            setLocale(this@UserProfileActivity, "ko")
            tvChooseLocale.text = resources.getString(R.string.korean)
        } else {
            tvChooseLocale.text = resources.getString(R.string.english)
        }

        if (captureImage == null) {
            captureImage = CaptureImage(this, this, getPackageName())
        }
        session = MyUtils.createSession(this)

        getUserDetail()
    }

    fun listener() {
        icBack.setOnClickListener {
            finish()
        }

        icEdit.setOnClickListener {
            if (MyUtils.allPermissionsGranted(this)) {
                captureImage?.selectImage()
            } else {
                ActivityCompat.requestPermissions(
                    this, MyUtils.REQUIRED_PERMISSIONS, MyUtils.REQUEST_CODE_PERMISSIONS
                )
            }
        }

        tvEditName.setOnClickListener {
            lineViewDisplay.visibility = View.GONE
            lineEditDisplay.visibility = View.VISIBLE
        }

        tvChangeName.setOnClickListener {
            lineViewDisplay.visibility = View.VISIBLE
            lineEditDisplay.visibility = View.GONE
            if (edtFirstName.text.toString().isNotEmpty()) {
                var firstName = ""
                var lastName = ""
                if (edtFirstName.text.toString().contains(" ")) {
                    lastName = edtFirstName.text.toString().substring(edtFirstName.text.toString().lastIndexOf(" "))
                    firstName = edtFirstName.text.toString().substring(0, edtFirstName.text.toString().lastIndexOf(" "))
                    updateName(firstName.trim(), lastName.trim())
                } else {
                    firstName = edtFirstName.text.toString()
                    updateName(firstName.trim(), lastName)
                }
            } else {
                tvUsername.text = detail?.body?.user?.full_name
                tvName.text = detail?.body?.user?.full_name
                edtFirstName.setText(detail?.body?.user?.full_name)
            }
        }

        tvChangePassword.setOnClickListener {
            changePassword()
        }

        tvEditPassword.setOnClickListener {
            lineViewPassword.visibility = View.GONE
            lineEditPassword.visibility = View.VISIBLE
        }

        tvSignOut.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            (application as LidarApp).prefManager!!.setToken("")
            (application as LidarApp).prefManager!!.setUserId("")
            (application as LidarApp).prefManager?.setTotalNotification("")
            (application as LidarApp).prefManager!!.getOrganizationId()?.let {
                (application as LidarApp).prefManager!!.setOrganizationId("")
            }
            (application as LidarApp).prefManager!!.getOrganizationRole()?.let {
                (application as LidarApp).prefManager!!.setOrganizationRole("")
            }
            startActivity(intent)
            finish()
        }

        tvUpgrade.setOnClickListener {
            val intent = Intent(this, PricingPlanActivity::class.java)
            intent.putExtra("update", "update")
            startActivity(intent)
        }

        tvCopy.setOnClickListener {
            try {
                val clipboard: ClipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied", tvOrganisationReference.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.text_copied),
                    Toast.LENGTH_SHORT).show();
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lineLocale.setOnClickListener {
            showPopupLocale(tvChooseLocale)
        }
    }

    override fun onResume() {
        super.onResume()
        footer.updateTabProfile((application as LidarApp).prefManager?.getTotalNotification()!!)
        footer.updateSession(session)
    }
    private fun changePassword() {
        if(MyUtils.Password_Validation(edtCurrentPassword.text.toString()) &&
            MyUtils.Password_Validation(edtNewPassword.text.toString()) &&
            MyUtils.Password_Validation(edtRepeatPassword.text.toString())) {
            if (edtNewPassword.text.toString().equals(edtRepeatPassword.text.toString())) {
                MyUtils.showProgress(this, flProgress)
                val changePassword = ChangePasswordRequest()
                changePassword.oldPassword = edtCurrentPassword.text.toString()
                changePassword.newPassword = edtNewPassword.text.toString()
                APIOpenAirUtils.changePassword((application as LidarApp).prefManager!!.getToken(),
                    changePassword, object : APIInterface.onDelegate {
                        override fun onSuccess(result: Any?) {
                            MyUtils.hideProgress(this@UserProfileActivity, flProgress)
                            val data = result as ResetPasswordResponse
                            lineViewPassword.visibility = View.VISIBLE
                            lineEditPassword.visibility = View.GONE
                            edtCurrentPassword.setText("")
                            edtNewPassword.setText("")
                            edtRepeatPassword.setText("")
                            Toast.makeText(
                                this@UserProfileActivity,
                                data.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onError(error: Any?) {
                            MyUtils.hideProgress(this@UserProfileActivity, flProgress)
                            MyUtils.toastError(this@UserProfileActivity, error as ErrorModel)
                        }
                    })
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.new_password_not_match),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                resources.getString(R.string.password_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateName(firstName: String?, lastName: String?) {
        MyUtils.showProgress(this, flProgress)
        val name = UpdateUserRequest()
        if (firstName?.isNotEmpty()!!) {
            name.first_name = firstName
        } else {
            name.first_name = detail?.body?.user?.first_name
        }

        if (lastName?.isNotEmpty()!!) {
            name.last_name = lastName
        } else {
            name.last_name = " "
        }

        APIOpenAirUtils.updateUser(
            (application as LidarApp).prefManager!!.getToken(),
            name, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@UserProfileActivity, flProgress)
                    val data = result as UserImageResponse
                    detail?.body?.user = data?.body
                    tvUsername.text = data?.body?.full_name
                    tvName.text = data?.body?.full_name
                    edtFirstName.setText(data?.body?.full_name)
                    (application as LidarApp).prefManager?.setFullName(
                        data?.body?.full_name!!
                    )
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@UserProfileActivity, flProgress)
                    MyUtils.toastError(this@UserProfileActivity, error as ErrorModel)
                }
            })
    }

    fun getUserDetail() {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getUserDetail((application as LidarApp).prefManager!!.getToken() , object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@UserProfileActivity, flProgress)
                detail = result as UserDetailResponse
                if (detail?.body?.user?.full_name != null) {
                    tvUsername.text = detail?.body?.user?.full_name
                    tvName.text = detail?.body?.user?.full_name
                }

                edtFirstName.setText(detail?.body?.user?.full_name)
//                edtLastName.setText(detail?.body?.user?.last_name)
                if (detail?.body?.user?.profileImageKey != null && detail?.body?.user?.profileImageKey != "") {

                    MyUtils.loadAvatar(this@UserProfileActivity, (application as LidarApp).prefManager!!.getToken(),
                        detail?.body?.user?.profileImageKey, avatar, resources.getDrawable(R.drawable.user))
                }

                tvSubType.text = (application as LidarApp).prefManager!!.getSubscriptionName()
                tvSubType1.text = (application as LidarApp).prefManager!!.getSubscriptionName()
                tvEmail.text = detail?.body?.user?.email
                detail?.body?.userSubscription?.expiryDate?.also {
                    tvDate.text = MyUtils.convertDateTime(it)
                }
                detail?.body?.userSubscription?.renewalDate?.also {
                    tvRenew.text = resources.getString(R.string.renewal_due_on).plus(" ").plus(MyUtils.convertDateTime(it))
                }

                detail?.body?.orgData?.let {
                    if (it.isNotEmpty()) {
                        lineOrganization.visibility = View.VISIBLE
                        footer.visibility = View.VISIBLE
                        for (org in it) {
                            org.role?.also {role ->
                                org.orgSubscription?.organization?.let { org ->
                                    tvOrganization.text = org.name.plus(" ("+ role.substring(0, 1).uppercase() + role.substring(1).lowercase()+")")
                                    tvOrganisationReference.text = org.referenceID
                                }
                                org.orgSubscription?.expiryDate?.also {
                                    tvDate.text = MyUtils.convertDateTime(it)
                                }
                                org.orgSubscription?.renewalDate?.also {
                                    tvRenew.text = getString(R.string.renewal_due_on).plus(" ").plus(MyUtils.convertDateTime(it))
                                }
                                if (role.contains("user")) {
                                    rlReference.visibility = View.GONE
                                    tvUpgrade.visibility = View.GONE
                                } else {
                                    rlReference.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }

                profile.visibility = View.VISIBLE
            }

            override fun onError(error: Any?) {
                profile.visibility = View.VISIBLE
                MyUtils.hideProgress(this@UserProfileActivity, flProgress)
                MyUtils.toastError(this@UserProfileActivity, error as ErrorModel)
            }
        })
    }

    //upload photo
    fun uploadAvatar(accessToken: String?, outputFile: Bitmap?) {
        MyUtils.showProgress(this, flProgress)
        val builder: MultipartBody.Builder =
            MultipartBody.Builder().setType(MultipartBody.FORM)
        val stream = ByteArrayOutputStream()
        outputFile?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        builder.addFormDataPart(
            "Image", "avatar",
            RequestBody.create("image/*".toMediaType(), byteArray)
        )

        val requestBody: RequestBody = builder.build()
        APIOpenAirUtils.uploadAvatar(
            accessToken!!,
            requestBody, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@UserProfileActivity, flProgress)
                val detail = result as UserImageResponse
                if (detail.body?.profileImageKey != null && detail.body?.profileImageKey != "") {
                    MyUtils.loadAvatar(this@UserProfileActivity, (application as LidarApp).prefManager!!.getToken(),
                        detail?.body?.profileImageKey, avatar, resources.getDrawable(R.drawable.user))
                    (application as LidarApp).prefManager?.setProfileImageKey(
                        detail?.body?.profileImageKey!!
                    )
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@UserProfileActivity, flProgress)
                MyUtils.toastError(this@UserProfileActivity, error as ErrorModel)
            }
        })
    }

    override fun onCaptureImageFromUrl(bitmap: Bitmap?) {

    }

    override fun onCaptureImageFromFile(bitmap: Bitmap?) {
        uploadAvatar((application as LidarApp).prefManager!!.getToken(), bitmap)
    }

    override fun onSelectFromGallery(bitmap: Bitmap?, string: String?, mimetype: String?) {
        uploadAvatar((application as LidarApp).prefManager!!.getToken(), bitmap)
    }

    override fun onRecordVideo(uri: Uri?) {

    }

    override fun onSelectFromGalleryMoreImgae(bitmap: Bitmap?) {

    }

    override fun onSelectImage(type: String) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        captureImage?.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        captureImage?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(captureImage != null) {
            captureImage = null
        }
        session?.let {
            session = null
        }
    }

    private fun showPopupLocale(anchor: View?) {

        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.popup_choose_locale, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }

            contentView.findViewById<TextView>(R.id.tvEnglish)
                .setOnClickListener {
                    setLocale(this@UserProfileActivity, "en")
                    (application as LidarApp).prefManager?.setLocale("en")
                    tvChooseLocale.text = resources.getString(R.string.english)
                    startActivity(Intent(this@UserProfileActivity, UserProfileActivity::class.java))
                    dismiss()

                }
            contentView.findViewById<TextView>(R.id.tvKorean)
                .setOnClickListener {
                    setLocale(this@UserProfileActivity, "ko")
                    (application as LidarApp).prefManager?.setLocale("ko")
                    tvChooseLocale.text = resources.getString(R.string.korean)
                    startActivity(Intent(this@UserProfileActivity, UserProfileActivity::class.java))
                    dismiss()

                }
        }.also { popupWindow ->
            popupWindow.height = LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            popupWindow.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT)
            )

            // Absolute location of the anchor view
            val location = IntArray(2).apply {
                anchor?.getLocationOnScreen(this)
            }
            val size = Size(
                popupWindow.contentView.measuredWidth,
                popupWindow.contentView.measuredHeight
            )
            popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.END,
                resources.getDimensionPixelOffset(R.dimen.size_26),
                location[1] - size.height+resources.getDimensionPixelOffset(R.dimen.size_26)+popupWindow.contentView.measuredHeight
            )
        }
    }

    fun setLocale(activity: Activity, languageCode: String?) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = activity.resources
        val config: Configuration = resources.getConfiguration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.getDisplayMetrics())
    }
}