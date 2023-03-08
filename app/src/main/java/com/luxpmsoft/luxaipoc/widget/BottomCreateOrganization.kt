package com.luxpmsoft.luxaipoc.widget

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.luxpmsoft.luxaipoc.R

class BottomCreateOrganization (private val listener: IAdapterClickListener) : BottomSheetDialogFragment() {
    private lateinit var lnCreateNew: LinearLayout
    private lateinit var lnJoinExisting: LinearLayout
    private lateinit var btCreate: TextView
    private lateinit var btCancel: TextView
    private lateinit var tvTitle: TextView
    private lateinit var edtOrganizationName: EditText
    private lateinit var radioCreateNew: RadioButton
    private lateinit var radioJoinExist: RadioButton
    private var disChoose: Int? = 0
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.dialog_create_organization, null)

        dialog.setContentView(view)
        dialog.window!!.findViewById<View>(R.id.design_bottom_sheet)
            .setBackgroundResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view =  inflater.inflate(R.layout.dialog_create_organization, container, false)
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(true)
        btCreate = view.findViewById(R.id.btCreate)
        btCancel = view.findViewById(R.id.btCancel)
        lnCreateNew = view.findViewById(R.id.lnCreateNew)
        lnJoinExisting = view.findViewById(R.id.lnJoinExisting)
        edtOrganizationName = view.findViewById(R.id.edtOrganizationName)
        radioCreateNew = view.findViewById(R.id.radioCreateNew)
        radioJoinExist = view.findViewById(R.id.radioJoinExist)
        tvTitle = view.findViewById(R.id.tvTitle)

        lnCreateNew.setOnClickListener {
            radioCreateNew.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.blue3))
            radioJoinExist.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            tvTitle.text = resources.getString(R.string.str_create_new_organization)
            edtOrganizationName.hint = resources.getString(R.string.str_enter_organization_name)
            btCreate.text = resources.getString(R.string.str_create)
            disChoose = 0
        }

        lnJoinExisting.setOnClickListener {
            radioJoinExist.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.blue3))
            radioCreateNew.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            tvTitle.text = resources.getString(R.string.str_join_existing_organization)
            edtOrganizationName.hint = resources.getString(R.string.str_enter_organization_id)
            btCreate.text = resources.getString(R.string.str_request)
            disChoose = 1
        }

        btCreate.setOnClickListener {
            if (edtOrganizationName.text.toString().isNotEmpty()) {
                listener.onClickListener(disChoose, edtOrganizationName.text.toString())
                dismiss()
            } else {
                Toast.makeText(requireActivity(), "Please enter organization name", Toast.LENGTH_SHORT).show()
            }
        }

        btCancel.setOnClickListener {
            dismiss()
        }

        return view
    }
}