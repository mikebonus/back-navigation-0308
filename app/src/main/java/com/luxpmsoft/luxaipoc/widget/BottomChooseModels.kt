package com.luxpmsoft.luxaipoc.widget

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.SelectModelsAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.recentmodel.ReconstructionResponse
import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import com.luxpmsoft.luxaipoc.model.recentmodel.request.CadFileRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils

class BottomChooseModels(private val listener: IAdapterClickListener,
                         val models: ArrayList<Rows>?, val boardId: String, val organId: String) : BottomSheetDialogFragment() {
    private lateinit var grvModels: RecyclerView
    private lateinit var icTick: ImageView
    private lateinit var lineInputFileName: FrameLayout
    private lateinit var lnChooseModels: FrameLayout
    private lateinit var btCreate: TextView
    private lateinit var edtFileName: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtSearch: EditText
    private lateinit var flProgress: FrameLayout
    private lateinit var icClear: ImageView
    var adapter: SelectModelsAdapter? = null
    var listModels: ArrayList<Rows>? = ArrayList()
    private val customHandler = Handler()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.dialog_select_model, null)

        dialog.setContentView(view)
        dialog.window!!.findViewById<View>(R.id.design_bottom_sheet)
            .setBackgroundResource(android.R.color.transparent)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view =  inflater.inflate(R.layout.dialog_select_model, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#5F5A94")))
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(true)
        grvModels = view.findViewById(R.id.grvModels)
        icTick = view.findViewById(R.id.icTick)
        lineInputFileName = view.findViewById(R.id.lineInputFileName)
        lnChooseModels = view.findViewById(R.id.lnChooseModels)
        btCreate = view.findViewById(R.id.btCreate)
        edtFileName = view.findViewById(R.id.edtFileName)
        edtDescription = view.findViewById(R.id.edtDescription)
        edtSearch = view.findViewById(R.id.edtSearch)
        icClear = view.findViewById(R.id.icClear)
        flProgress = view.findViewById(R.id.flProgress)
        icTick.setOnClickListener {
            for (file in listModels!!) {
                if (file.isCheck == true) {
                    lineInputFileName.visibility = View.VISIBLE
                    lnChooseModels.visibility = View.GONE
                    break
                }
            }

        }

        btCreate.setOnClickListener {
            if (edtFileName.text.toString().trim().isNotEmpty()) {
                addFile(edtFileName.text.trim().toString(), edtDescription.text.trim().toString())
            } else {
                Toast.makeText(requireActivity(), "Input file name", Toast.LENGTH_SHORT).show()
            }
        }

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                customHandler.removeCallbacks(getFile)
                customHandler.postDelayed(getFile, 600)
                s?.let {
                    if (it.isNotEmpty()) {
                        icClear.visibility = View.VISIBLE
                    } else {
                        icClear.visibility = View.INVISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        icClear.setOnClickListener {
            edtSearch.setText("")
        }

        initRecyclerView()

        return view
    }

    private val getFile: Runnable = object : Runnable {
        override fun run() {
            getReconstruction("All", edtSearch.text.toString())
        }
    }

    fun initRecyclerView() {
        models?.let {
            for (m in it) {
                m.isCheck = false
                listModels?.add(m)
            }
        }
        Utils.gridLayoutManager(activity, grvModels, 3, GridLayoutManager.VERTICAL)
        adapter = SelectModelsAdapter(requireActivity(), R.layout.item_select_model, listModels!!, object :
            SelectModelsAdapter.IAdapterClickListener {
            override fun onSelect(position: Int, isCheck: Boolean) {
                try {
                    listModels?.forEachIndexed { index, element ->
                        if (index == position) {
                            listModels?.get(index)?.isCheck =
                                listModels?.get(index)?.isCheck != true
                        } else {
                            listModels?.get(index)?.isCheck = false
                        }
                    }
                    adapter?.notifyDataSetChanged()
                } catch (e:Exception) {
                    e.message
                }
            }
        })
        grvModels.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    fun addFile(fileName: String?, description: String) {
        MyUtils.showProgress(requireActivity(), flProgress)
        var reconstructions: ArrayList<String> = ArrayList()
        for (file in listModels!!) {
            if (file.isCheck == true) {
                file.reconstructionID?.let { reconstructions.add(it) }
            }
        }
        val fileRequest = CadFileRequest()
        fileRequest.boardId = boardId
        fileRequest.name = fileName
        fileRequest.description = description
        fileRequest.reconstructions = reconstructions
        APIOpenAirUtils.addFile((activity?.application as LidarApp).prefManager!!.getToken(),
            organId, fileRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(requireActivity(), flProgress)
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.create_file_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                    listener.onClickListener(boardId.toInt())
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(requireActivity(), flProgress)
                    MyUtils.toastError(requireActivity(), error as ErrorModel)
                    dismiss()
                }
            })
    }

    fun getReconstruction(filter: String, search: String) {
        MyUtils.showProgress(requireActivity(), flProgress)
        var id = ""
        if ((activity?.application as LidarApp).prefManager?.getOrganizationRole() == "admin") {
            id = (activity?.application as LidarApp).prefManager?.getOrganizationId().toString()
        }
        APIOpenAirUtils.getReconstruction((activity?.application as LidarApp).prefManager!!.getToken(), null, null,
            null, null, filter, id, search, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(requireActivity(), flProgress)
                    val data = result as ReconstructionResponse
                    data.body?.let {
                        listModels?.clear()
                        listModels?.addAll(data.body?.rows!!)
                        adapter?.notifyDataSetChanged()
                        grvModels.smoothScrollToPosition(0)
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(requireActivity(), flProgress)
                    MyUtils.toastError(requireActivity(), error as ErrorModel)
                }
            })
    }
}