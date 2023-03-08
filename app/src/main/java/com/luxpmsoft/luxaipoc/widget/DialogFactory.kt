package com.luxpmsoft.luxaipoc.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.ar.core.CameraConfig
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.CountryAdapter
import com.luxpmsoft.luxaipoc.adapter.QualityAdapter
import com.luxpmsoft.luxaipoc.adapter.ResolutionAdapter
import com.luxpmsoft.luxaipoc.adapter.ViewPagerAdapter
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.model.Country
import com.luxpmsoft.luxaipoc.model.ResolutionModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ExerciseCategory
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel
import com.luxpmsoft.luxaipoc.model.user.Organization
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.BaseActivity

class DialogFactory {
    companion object {
        interface DialogListener {

            interface RetryNetListener {
                fun retry(dialog: TranslucentDialog?)
            }

            interface ViewModelListener {
                fun share()
                fun copyLink()
                fun download()
                fun renameModel()
                fun deleteModel()
            }

            interface RenameModel {
                fun renameModel(newName: String)
            }

            interface Delete {
                fun delete()
            }

            interface OtpVerification {
                fun otpVerification(code: String)
                fun onResendCode()
            }


            interface CreateRepo {
                fun createRepo(name: String, isPublic: String)
            }

            interface CreateBoard {
                fun createBoard(name: String, type:String)
            }

            interface CreateWorkspace {
                fun createWorkspace(name: String, description: String)
            }

            interface CreateProject {
                fun createProject(name: String)
            }

            interface Resolution {
                fun resolution(cameraConfig: CameraConfig)
            }

            interface Quality {
                fun quality(position: Int)
            }

            interface Scan {
                fun sceneScan()
                fun objectScan()
            }

            interface CountryListener {
                fun country(country: Country)
            }

            interface ScanObject {
                fun continueScan()
                fun uploadObject()
            }

            interface AddTag {
                fun addTag(name: String)
            }

            interface Rerecord {
                fun reRecord()
            }

            interface Retrain {
                fun reTrain()
            }

            interface Workout {
                fun categoryName(name: String, id: String?)
            }

            interface TestDetection {
                fun show()
                fun goHome()
            }
        }

        fun createTryNetAgainDialog(
            context: BaseActivity,
            message: String?,
            listener: DialogListener.RetryNetListener
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_retry_net)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val tvMessage = dialog.findViewById(R.id.txtContent) as TextView
            tvMessage.text = message
            val btTryNet = dialog.findViewById(R.id.btTryNet) as TextView
            val btSetup = dialog.findViewById(R.id.btSetup) as TextView
            btSetup.setOnClickListener {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))

//                Intent intent = new Intent(Intent.ACTION_MAIN, null);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
//                intent.setComponent(cn);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
            }
            btTryNet.setOnClickListener { listener.retry(dialog) }
            if (!context.isFinishing()) {
                //show dialog
                dialog.show()
            }
        }

        fun otpVerification(
            context: Context,
            listener: DialogListener.OtpVerification
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_otp_verification)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val otpCode = dialog.findViewById(R.id.squareField) as SquarePinField
            val btConfirm = dialog.findViewById(R.id.tvVerify) as TextView
            val btCancel = dialog.findViewById(R.id.tvCancel) as TextView
            val btResend = dialog.findViewById(R.id.tvResendCode) as TextView
            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btConfirm.setOnClickListener {
                if (otpCode.text.toString().isNotEmpty()) {
                    listener.otpVerification(otpCode.text.toString())
                    dialog.dismiss()
                }
            }

            btResend.setOnClickListener {
                listener.onResendCode()
            }
            dialog.show()
        }

        fun dialogRenameModel(
            context: Context,
            listener: DialogListener.RenameModel,
            text: String
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_rename_model)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val txtContent = dialog.findViewById(R.id.txtContent) as TextView
            val btCancel = dialog.findViewById(R.id.btCancel) as TextView
            val btConfirm = dialog.findViewById(R.id.btConfirm) as TextView
            val edtName = dialog.findViewById(R.id.edtName) as TextView
            txtContent.setText(text)
            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btConfirm.setOnClickListener {
                dialog.dismiss()
                if(edtName.text.toString().isNotEmpty()) {
                    listener.renameModel(edtName.text.toString())
                }
            }
            dialog.show()
        }

        fun dialogDelete(
            context: Context,
            listener: DialogListener.Delete,
            title: String?) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_delete)
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.CENTER)
            wmlp.gravity = Gravity.CENTER
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val btnCancel = dialog.findViewById(R.id.btnCancel) as TextView
            val btnYes = dialog.findViewById(R.id.btnYes) as TextView
            val txtContent = dialog.findViewById(R.id.txtContent) as TextView
            txtContent.text = context.resources.getString(R.string.str_want_delete).plus(title)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnYes.setOnClickListener {
                dialog.dismiss()
                listener.delete()
            }
            dialog.show()
        }

        fun dialogConfirmDelete(
            context: Context,
            listener: DialogListener.Delete,
            title: String?) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_confirm_delete)
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.CENTER)
            wmlp.gravity = Gravity.CENTER
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val btnCancel = dialog.findViewById(R.id.btnCancel) as TextView
            val btnYes = dialog.findViewById(R.id.btnYes) as TextView
            val txtContent = dialog.findViewById(R.id.txtContent) as TextView
            txtContent.text = context.resources.getString(R.string.str_want_confirm_delete).plus(title)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnYes.setOnClickListener {
                dialog.dismiss()
                listener.delete()
            }
            dialog.show()
        }

        fun dialogCreateRepo(
            context: Context,
            listener: DialogListener.CreateRepo
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_create_repo)
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.CENTER)
            wmlp.gravity = Gravity.CENTER
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val btCancel = dialog.findViewById(R.id.btCancel) as TextView
            val btCreate = dialog.findViewById(R.id.btCreate) as TextView
            val edtRepoName = dialog.findViewById(R.id.edtRepoName) as EditText
            val spnAccess = dialog.findViewById(R.id.spnAccess) as DropdownSelectInput
            var dropdownList: ArrayList<Organization>? = ArrayList()
            val or = Organization()
            or.name = context.resources.getString(R.string.repo_public)
            or.icon = context.resources.getDrawable(R.drawable.ic_public)
            dropdownList?.add(or)
            val or1 = Organization()
            or1.name = context.resources.getString(R.string.repo_private)
            or1.icon = context.resources.getDrawable(R.drawable.ic_private)
            dropdownList?.add(or1)
            spnAccess.setData(dropdownList, true)
            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btCreate.setOnClickListener {
                if (edtRepoName.text.toString().trim().isNotEmpty()) {
                    dialog.dismiss()
                    if (spnAccess.getNames().contains("Public")) {
                        listener.createRepo(edtRepoName.text.toString().trim(), "true")
                    } else {
                        listener.createRepo(edtRepoName.text.toString().trim(), "false")
                    }
                } else {
                    Toast.makeText(context, "Name is empty", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        }

        fun dialogCreateBoard(
            context: Context,
            listener: DialogListener.CreateBoard
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_create_board)
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.CENTER)
            wmlp.gravity = Gravity.CENTER
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val btCancel = dialog.findViewById(R.id.btCancel) as TextView
            val btCreate = dialog.findViewById(R.id.btCreate) as TextView
            val edtBoardName = dialog.findViewById(R.id.edtBoardName) as EditText
            val spnBoardType = dialog.findViewById(R.id.spnBoardType) as DropdownSelectInput
            var dropdownList: ArrayList<Organization>? = ArrayList()
            val or = Organization()
            or.name = "Project"
            dropdownList?.add(or)
            val or1 = Organization()
            or1.name = "Cadfile"
            dropdownList?.add(or1)
            spnBoardType.setData(dropdownList, false)
            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btCreate.setOnClickListener {
                if (edtBoardName.text.toString().trim().isNotEmpty()) {
                    dialog.dismiss()
                    listener.createBoard(edtBoardName.text.trim().toString(), spnBoardType.getNames())
                } else {
                    Toast.makeText(context, "Name is empty", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        }

        fun dialogCreateWorkspace(
            context: Context,
            listener: DialogListener.CreateWorkspace
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_create_workspace)
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.CENTER)
            wmlp.gravity = Gravity.CENTER
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val btCancel = dialog.findViewById(R.id.btCancel) as TextView
            val btCreate = dialog.findViewById(R.id.btCreate) as TextView
            val edtWorkspaceName = dialog.findViewById(R.id.edtWorkspaceName) as EditText
            val edtDescription = dialog.findViewById(R.id.edtDescription) as EditText
            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btCreate.setOnClickListener {
                if (edtWorkspaceName.text.toString().trim().isNotEmpty() && edtDescription.text.toString().trim().isNotEmpty()) {
                    dialog.dismiss()
                    listener.createWorkspace(edtWorkspaceName.text.toString(), edtDescription.text.trim().toString())
                } else {
                    Toast.makeText(context, "Name or description is empty", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        }

        fun dialogCreateProject(
            context: Context,
            listener: DialogListener.CreateProject
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_create_project)
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.CENTER)
            wmlp.gravity = Gravity.CENTER
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val btCancel = dialog.findViewById(R.id.btCancel) as TextView
            val btCreate = dialog.findViewById(R.id.btCreate) as TextView
            val edtProjectName = dialog.findViewById(R.id.edtProjectName) as EditText
            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btCreate.setOnClickListener {
                if (edtProjectName.text.toString().trim().isNotEmpty()) {
                    dialog.dismiss()
                    listener.createProject(edtProjectName.text.trim().toString())
                } else {
                    Toast.makeText(context, "Name is empty", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        }

        fun dialogNoticeScan(
            context: Context,
            listener: DialogListener.ScanObject
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_notice_scan_object)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val btContinue = dialog.findViewById(R.id.btContinue) as TextView
            val btUpload = dialog.findViewById(R.id.btUpload) as TextView
            btContinue.setOnClickListener {
                dialog.dismiss()
                listener.continueScan()
            }

            btUpload.setOnClickListener {
                dialog.dismiss()
                listener.uploadObject()
            }
            dialog.show()
        }

        fun dialogView(
            context: Activity,
            listener: DialogListener.ViewModelListener
        ) {
//            val dialog = Dialog(context, R.style.DialogSlideAnim)
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_view_model)
            dialog.getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.BOTTOM)
            wmlp.gravity = Gravity.BOTTOM
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val rlShare = dialog.findViewById(R.id.rlShare) as LinearLayout
            val rlCopyLink = dialog.findViewById(R.id.rlCopyLink) as LinearLayout
            val rlDownload = dialog.findViewById(R.id.rlDownload) as LinearLayout
            val tvRename = dialog.findViewById(R.id.tvRename) as TextView
            val tvDelete = dialog.findViewById(R.id.tvDelete) as TextView
            val tvCancel = dialog.findViewById(R.id.tvCancel) as TextView

            rlShare.setOnClickListener {
                dialog.dismiss()
                listener.share()
            }

            rlCopyLink.setOnClickListener {
                dialog.dismiss()
                listener.copyLink()
            }

            rlDownload.setOnClickListener {
                dialog.dismiss()
                listener.download()
            }

            tvRename.setOnClickListener {
                dialog.dismiss()
                listener.renameModel()
            }

            tvDelete.setOnClickListener {
                dialog.dismiss()
                listener.deleteModel()
            }

            tvCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        fun dialogChooseScan(
            context: Activity,
            listener: DialogListener.Scan
        ) {
            val dialog = Dialog(context, R.style.DialogSlideAnim)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            dialog.setContentView(R.layout.dialog_choose_scan)
            dialog.getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            val wmlp = dialog.window!!.attributes
            wmlp.gravity = Gravity.BOTTOM
            wmlp.y = 170
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val lineScene = dialog.findViewById(R.id.lineScene) as LinearLayoutCompat
            val lineObject = dialog.findViewById(R.id.lineObject) as LinearLayoutCompat

            lineScene.setOnClickListener {
                dialog.dismiss()
                listener.sceneScan()
            }

            lineObject.setOnClickListener {
                dialog.dismiss()
                listener.objectScan()
            }

            dialog.show()
        }

        fun dialogResolution(
            context: Activity,
            listener: DialogListener.Resolution,
            cameraConfig: ArrayList<CameraConfig>
        ) {
            val dialog = Dialog(context, R.style.DialogSlideAnim)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            dialog.setContentView(R.layout.dialog_resolution)
            dialog.getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.BOTTOM)
            wmlp.gravity = Gravity.BOTTOM
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val grvResolution = dialog.findViewById(R.id.grvResolution) as RecyclerView
            val resolutionModels = ArrayList<ResolutionModel>()
            Utils.gridLayoutManager(context, grvResolution, 1, GridLayoutManager.VERTICAL)
            val resolutionAdapter: ResolutionAdapter? = ResolutionAdapter(context, R.layout.item_resolution, cameraConfig, object :
                ResolutionAdapter.IAdapterClickListener {
                override fun onClickListener(cameraConfig: CameraConfig) {
                    listener.resolution(cameraConfig)
                    dialog.dismiss()
                }
            })
            grvResolution.adapter = resolutionAdapter
            resolutionAdapter?.notifyDataSetChanged()

            dialog.show()
        }

        fun dialogQuality(
            context: Activity,
            listener: DialogListener.Quality,
            quality: ArrayList<String>
        ) {
            val dialog = Dialog(context, R.style.DialogSlideAnim)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            dialog.setContentView(R.layout.dialog_resolution)
            dialog.getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.BOTTOM)
            wmlp.gravity = Gravity.BOTTOM
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val grvResolution = dialog.findViewById(R.id.grvResolution) as RecyclerView
            val tvTitle = dialog.findViewById(R.id.tvTitle) as TextView
            tvTitle.text = "Choose quality"
            Utils.gridLayoutManager(context, grvResolution, 1, GridLayoutManager.VERTICAL)
            val qualityAdapter: QualityAdapter? = QualityAdapter(context, R.layout.item_resolution, quality, object :
                QualityAdapter.IAdapterClickListener {
                override fun onClickListener(position: Int) {
                    listener.quality(position)
                    dialog.dismiss()
                }
            })
            grvResolution.adapter = qualityAdapter
            qualityAdapter?.notifyDataSetChanged()

            dialog.show()
        }

        fun dialogChooseCountry(
            context: Activity,
            listener: DialogListener.CountryListener
        ) {
            val dialog = Dialog(context, R.style.DialogSlideAnim)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            dialog.setContentView(R.layout.dialog_choose_country)
            dialog.getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.BOTTOM)
            wmlp.gravity = Gravity.BOTTOM
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val search = dialog.findViewById(R.id.searchView) as SearchView
            val grvCountry = dialog.findViewById(R.id.rv_country_code) as RecyclerView
            var list: ArrayList<Country>? = MyUtils.getCountriesList(context)
            Utils.gridLayoutManager(context, grvCountry, 1, GridLayoutManager.VERTICAL)
            val countryAdapter: CountryAdapter? = CountryAdapter(context, R.layout.item_select_country,
                list!!, object :
                CountryAdapter.OnListener {
                    override fun onListener(country: Country) {
                        listener.country(country)
                        dialog.dismiss()
                    }
            })
            grvCountry.adapter = countryAdapter
            countryAdapter?.notifyDataSetChanged()
            dialog.show()
        }

        fun dialogAddTag(
            context: Context,
            listener: DialogListener.AddTag
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.setContentView(R.layout.dialog_create_tag)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.CENTER)
            wmlp.gravity = Gravity.CENTER
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val btCancel = dialog.findViewById(R.id.btCancel) as TextView
            val btAdd = dialog.findViewById(R.id.btAdd) as TextView
            val edtTagName = dialog.findViewById(R.id.edtTagName) as EditText
            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btAdd.setOnClickListener {
                if (edtTagName.text.toString().isNotEmpty()) {
                    dialog.dismiss()
                    listener.addTag(edtTagName.text.toString())
                } else {
                    Toast.makeText(context, "Tag name is empty", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        }

        fun dialogRerecord(
            context: Context,
            listener: DialogListener.Rerecord
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_re_record)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val btCancel = dialog.findViewById(R.id.btCancel) as TextView
            val btRecord = dialog.findViewById(R.id.btRecord) as TextView
            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btRecord.setOnClickListener {
                dialog.dismiss()
                listener.reRecord()
            }

            dialog.show()
        }

        fun dialogConfirmDeleteFrame(
            context: Context,
            listener: DialogListener.Rerecord
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_confirm_delete_frame)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val btCancel = dialog.findViewById(R.id.btCancel) as TextView
            val btDelete = dialog.findViewById(R.id.btDelete) as TextView
            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btDelete.setOnClickListener {
                dialog.dismiss()
                listener.reRecord()
            }

            dialog.show()
        }

        fun dialogRetrainSuccess(
            context: Context,
            listener: DialogListener.Retrain,
            message: String?
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_retrain_success)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val btGoHome = dialog.findViewById(R.id.btGoHome) as TextView
            val txtContent = dialog.findViewById(R.id.txtContent) as TextView
            txtContent.text = message
            btGoHome.setOnClickListener {
                dialog.dismiss()
                listener.reTrain()
            }

            dialog.show()
        }

        fun dialogInputCategoryWorkout(
            context: Context,
            listener: DialogListener.Workout,
            category: ArrayList<ExerciseCategory>
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_input_category_workout)
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val wmlp = dialog.window!!.attributes
            dialog.window!!.setGravity(Gravity.CENTER)
            wmlp.gravity = Gravity.CENTER
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val btCancel = dialog.findViewById(R.id.btCancel) as TextView
            val btUpload = dialog.findViewById(R.id.btUpload) as TextView
            val tvAddNew = dialog.findViewById(R.id.tvAddNew) as TextView
            val lineInputCategory = dialog.findViewById(R.id.lineInputCategory) as LinearLayoutCompat
            val lineChooseCategory = dialog.findViewById(R.id.lineChooseCategory) as LinearLayoutCompat
            val spnCategory = dialog.findViewById(R.id.spnCategory) as DropdownSelectInput
            val edtCategoryName = dialog.findViewById(R.id.edtCategoryName) as EditText
            if (category.isNotEmpty()) {
                var dropdownList: ArrayList<Organization>? = ArrayList()
                for (cate in category) {
                    val or = Organization()
                    or.name = cate.workoutCategoryName
                    or.referenceID = cate.categoryID
                    dropdownList?.add(or)
                }
                spnCategory.setData(dropdownList, false)
            } else {
                lineInputCategory.visibility = View.VISIBLE
                lineChooseCategory.visibility = View.GONE
            }

            btCancel.setOnClickListener {
                dialog.dismiss()
            }

            btUpload.setOnClickListener {
                if (lineChooseCategory.visibility == View.VISIBLE) {
                    dialog.dismiss()
                    listener.categoryName(spnCategory.getNames(), spnCategory.getID())
                } else {
                    if (edtCategoryName.text.toString().trim().isNotEmpty()) {
                        dialog.dismiss()
                        listener.categoryName(edtCategoryName.text.trim().toString(), null)
                    } else {
                        Toast.makeText(context, "Name is empty", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            tvAddNew.setOnClickListener {
                lineInputCategory.visibility = View.VISIBLE
                lineChooseCategory.visibility = View.GONE
            }
            dialog.show()
        }

        fun dialogTestDetection(
            context: Context,
            listener: DialogListener.TestDetection,
            isStatus: Int = 0
        ) {
            val dialog = TranslucentDialog(context)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_test_detection)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val btnOK = dialog.findViewById(R.id.btnOK) as TextView
            val lineDF = dialog.findViewById(R.id.lineDF) as LinearLayout
            val btnShowResult = dialog.findViewById(R.id.btnShowResult) as TextView
            val btnCancel = dialog.findViewById(R.id.btnCancel) as TextView
            val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
            val txtDescription = dialog.findViewById(R.id.txtDescription) as TextView

            if (isStatus == 0) {
                lineDF.visibility = View.GONE
                btnOK.visibility = View.VISIBLE
                txtTitle.text = context.getString(R.string.str_title_no_defect)
                txtDescription.text = context.getString(R.string.str_description_no_defect)
            } else {
                lineDF.visibility = View.VISIBLE
                btnOK.visibility = View.GONE
                txtTitle.text = context.getString(R.string.str_title_defect)
                txtDescription.text = context.getString(R.string.str_description_defect)
            }
            btnOK.setOnClickListener {
                dialog.dismiss()
                listener.goHome()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
                listener.goHome()
            }

            btnShowResult.setOnClickListener {
//                dialog.dismiss()
                listener.show()
            }

            dialog.show()
        }

        fun dialogShowImage(
            context: Activity,
            imagePaths: ArrayList<ImageFrameModel>,
            path: String,
            position: Int
        ) {
            val dialog = Dialog(context,R.style.DialogSlideAnim)
//        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog.setContentView(R.layout.dialog_show_full_image)
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val icClose = dialog.findViewById(R.id.icClose) as ImageView
            val viewPager = dialog.findViewById(R.id.viewPager) as ViewPager
            var framesAdapter: ViewPagerAdapter? = ViewPagerAdapter(context,imagePaths, path, true, onListener = object :
                ViewPagerAdapter.OnListener {
                override fun onDrawListener(position: Int, isDraw: Boolean?) {

                }
            })
            viewPager.adapter = framesAdapter
            viewPager.currentItem = position
            viewPager.offscreenPageLimit =imagePaths.size
//            Utils.loadImage(context, url, image)
//            image.visibility = View.VISIBLE
            icClose.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setOnKeyListener { dialog, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action === KeyEvent.ACTION_UP) {
                    dialog.dismiss()
                }
                true
            }
            dialog.show()
        }
    }
}