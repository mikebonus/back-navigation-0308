package com.luxpmsoft.luxaipoc.widget

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.SelectFileAdapter
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.model.recentmodel.CadFileRows

class BottomChooseFile(private val listener: IAdapterClickListener,
                        val models: ArrayList<CadFileRows>?, val boardId: String) : BottomSheetDialogFragment() {
    private lateinit var grvFile: RecyclerView
    var adapter: SelectFileAdapter? = null
    var btCancel: TextView? = null
    var btDelete: TextView? = null
    var listModels: ArrayList<CadFileRows>? = ArrayList()
    var listChoose: ArrayList<CadFileRows>? = ArrayList()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.dialog_select_file, null)

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
        val view =  inflater.inflate(R.layout.dialog_select_file, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#5F5A94")))
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(true)
        grvFile = view.findViewById(R.id.grvFile)
        btCancel = view.findViewById(R.id.btCancel)
        btDelete = view.findViewById(R.id.btDelete)
        btCancel?.setOnClickListener {
            dismiss()
        }
        btDelete?.setOnClickListener {
            listModels?.forEachIndexed { index, element ->
                if (element.isCheck == true) {
                    listChoose?.add(element)
                }
            }

            listener.onClickListener(boardId.toInt(), listChoose)
            dismiss()
        }

        initRecyclerView()

        return view
    }

    fun initRecyclerView() {
        models?.let { listModels?.addAll(it) }
        Utils.gridLayoutManager(activity, grvFile, 3, GridLayoutManager.VERTICAL)
        adapter = SelectFileAdapter(requireActivity(), R.layout.item_select_model, listModels!!, object :
            SelectFileAdapter.IAdapterClickListener {
            override fun onSelect(position: Int, isCheck: Boolean) {
                try {
                    listModels?.get(position)?.isCheck = isCheck
                } catch (e:Exception) {
                    e.message
                }
            }
        })
        grvFile.adapter = adapter
    }
}