package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.BoardAdapter
import com.luxpmsoft.luxaipoc.adapter.RecentFileAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.recentmodel.*
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.*
import kotlinx.android.synthetic.main.activity_file.*

class BoardFileManagerActivity: BaseActivity(), RecentFileAdapter.OnListener, BoardAdapter.OnListener {
    var recentFileAdapter: RecentFileAdapter? = null
    var recentFileModel: ArrayList<Rows>? = ArrayList()
    var recentFile: ArrayList<CadFileRows>? = ArrayList()
    var boardAdapter: BoardAdapter? = null
    var boardModel: ArrayList<CadFileRows>? = ArrayList()
    var boardId = ""
    var boardName = ""
    var total = 0
    var isFirst = false
    var index = 0
    var indexLoad = 0
    var pageCadIndex = 0
    var pageCadSize = 10
    var totalCad = 0
    var isFirstCad = false
    var listModels :ArrayList<CadFileRows>? = ArrayList()
    private val customHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        setContentView(R.layout.activity_file)

        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        bundle?.let {
            boardId = it.getString("boardId").toString()
            boardName = it.getString("boardName").toString()
            tvBoardName.text = boardName
            tvProjectName.text = it.getString("projectName").toString()
        }

//        Utils.gridLayoutManager(this, grvRecentFile, 1, GridLayoutManager.HORIZONTAL)
//        recentFileAdapter = RecentFileAdapter(this, R.layout.item_recent_file, recentFile!!, this)
//        grvRecentFile.adapter = recentFileAdapter

        Utils.gridLayoutManager(this, grvBroad, 1, GridLayoutManager.VERTICAL)
        boardAdapter = BoardAdapter(this, R.layout.item_board, boardModel!!, this)
        grvBroad.adapter = boardAdapter
    }

    override fun onResume() {
        super.onResume()
        recentFileModel?.let {
            it.clear()
        }
        isFirst = false
        getReconstruction("All")
        resetBoard(true)
    }

    override fun onDestroy() {
        super.onDestroy()
//        recentFileAdapter?.let {
//            recentFileAdapter = null
//        }
        recentFileModel?.let {
            recentFileModel = null
        }
//        recentFile?.let {
//            recentFile = null
//        }
        boardAdapter?.let {
            boardAdapter = null
        }
        boardModel?.let {
            boardModel = null
        }
        listModels?.let {
            listModels = null
        }
    }

    fun listener() {
        icBack.setOnClickListener {
            finish()
        }

        grvRecentFile.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(isFirstCad && boardModel?.size!! < totalCad && !recyclerView.canScrollVertically(1)) {
                    pageCadIndex++
                    getCadFile(pageCadIndex, pageCadSize, edtSearchModel.text.toString(), true, false)
                }
            }
        })

        grvBroad.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(isFirstCad && boardModel?.size!! < totalCad && !recyclerView.canScrollVertically(1)) {
                    pageCadIndex++
                    getCadFile(pageCadIndex, pageCadSize, edtSearchModel.text.toString(), true, false)
                }
            }
        })

        lineCreateFile.setOnClickListener {
            lineCreateFile.isEnabled = false
            val menu = BottomChooseModels(object : IAdapterClickListener {
                    override fun onClickListener(id: Int?, obj: Any?) {
                        boardAdapter?.let {
                            //reset value position edit cad file
                            boardModel?.stream()?.forEach { elt -> elt.isRemove = false }
                            it.selectedPosition = -1
                            it.lastPosition = -1
                            it.notifyDataSetChanged()
                        }
                        resetBoard(true)
                    }
                }, recentFileModel, boardId, (application as LidarApp).prefManager!!.getOrganizationId())
            menu.show(supportFragmentManager, menu.tag)
            Handler().postDelayed({
                lineCreateFile.isEnabled = true
            }, 1500)
        }

        icOptions.setOnClickListener {
            showPopupWindow(icOptions)
        }

        edtSearchModel.addTextChangedListener(object : TextWatcher {
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
            edtSearchModel.setText("")
        }

        tvCancel.setOnClickListener {
            lineDelete.visibility = View.GONE
//            icOptions.visibility = View.VISIBLE
            lineOptions.visibility = View.VISIBLE
            boardAdapter?.let {
                boardModel?.stream()?.forEach { elt -> elt.isRemove = false }
                it.isRemove = false
                it.notifyDataSetChanged()
            }
        }

        tvCancelEdit.setOnClickListener {
            tvCancelEdit.visibility = View.GONE
//            icOptions.visibility = View.VISIBLE
            lineOptions.visibility = View.VISIBLE
            boardAdapter?.let {
                //reset value position edit cad file
                it.selectedPosition = -1
                it.lastPosition = -1
                it.isEdit = false
                it.notifyDataSetChanged()
            }
        }

        tvDelete.setOnClickListener {
            listModels?.clear()
            boardModel?.forEachIndexed { index, element ->
                if (element.isRemove == true) {
                    listModels?.add(element)
                }
            }
            if (listModels!!.isNotEmpty()) {
                DialogFactory.dialogDelete(
                    this@BoardFileManagerActivity,
                    object : DialogFactory.Companion.DialogListener.Delete {
                        override fun delete() {
                            listModels?.let {
                                for (file in listModels!!) {
                                    file.cadFileID?.let { it1 -> deleteFiles(it1) }
                                }
                            }
                        }
                    }, " ".plus(listModels?.size.toString().plus(" files ")))
            }
        }
    }

    override fun onListener(model: String) {

    }

    private val getFile: Runnable = object : Runnable {
        override fun run() {
            resetBoard(false)
//            MyUtils.hideKeyboard(this@BoardFileManagerActivity)
        }
    }

    fun resetBoard(isLoading: Boolean) {
        pageCadIndex = 0
        indexLoad = 0
        isFirstCad = false
        getCadFile(pageCadIndex, pageCadSize, edtSearchModel.text.toString(), isLoading, true)
    }

    private fun showPopupWindow(anchor: View?) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.dialog_board_options, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }
            contentView.findViewById<LinearLayoutCompat>(R.id.lineEditInfo)
                .setOnClickListener(View.OnClickListener {
                    boardAdapter?.isEdit = true
                    boardAdapter?.notifyDataSetChanged()
                    tvCancelEdit.visibility = View.VISIBLE
//                    icOptions.visibility = View.GONE
                    lineOptions.visibility = View.GONE
                    dismiss()
                })

            contentView.findViewById<LinearLayoutCompat>(R.id.lineDeleteFile)
                .setOnClickListener(View.OnClickListener {
                    if (boardModel!!.size > 0) {
                        lineDelete.visibility = View.VISIBLE
//                        icOptions.visibility = View.GONE
                        lineOptions.visibility = View.GONE
                        boardAdapter?.isRemove = true
                        boardAdapter?.notifyDataSetChanged()
//                        val menu = BottomChooseFile(
//                            object : IAdapterClickListener {
//                                override fun onClickListener(id: Int?, obj: Any?) {
//                                    index = 0
//                                    listModels = obj as ArrayList<CadFileRows>?
//                                    listModels?.let {
//                                        for (file in listModels!!) {
//                                            file.cadFileID?.let { it1 -> deleteFiles(it1) }
//                                        }
//                                    }
//                                }
//                            }, boardModel, boardId)
//
//                        menu.show(supportFragmentManager, menu.tag)

                    }

                    dismiss()
                })
        }.also { popupWindow ->
            popupWindow.width = resources.getDimension(R.dimen.size_180).toInt()
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

    @SuppressLint("NewApi")
    override fun onBoardListener(model: String) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                MyUtils.startSettingExternal(this)
//            } else {
//                startDetail(model)
//            }
//        } else {
            startDetail(model)
//        }
    }

    override fun onMore(model: String, view: View?, position: Int) {
        showPopupMore(view, model, position)
    }

    override fun onSelect(position: Int, isCheck: Boolean) {
        boardModel?.get(position)?.isRemove = isCheck
        boardAdapter?.notifyItemChanged(position)
    }

    override fun onEdit(position: Int, lastPosition:Int, isCheck: Boolean) {
        boardModel?.forEachIndexed { index, element ->
            if (index == position) {
                boardModel?.get(index)?.isEdit =
                    boardModel?.get(index)?.isEdit != true
            } else {
                boardModel?.get(index)?.isEdit = false
            }
        }
        boardAdapter?.notifyDataSetChanged()
        val intent = Intent(this@BoardFileManagerActivity, EditCadFileActivity::class.java)
        intent.putExtra("cadfile", Gson().toJson(boardModel?.get(position)))
        startActivity(intent)
    }

    private fun showPopupMore(anchor: View?, cadFileId: String, position: Int) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.popup_cad_file, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }
            contentView.findViewById<TextViewFonts>(R.id.tvEditInfo)
                .setOnClickListener(View.OnClickListener {
                    val intent = Intent(this@BoardFileManagerActivity, EditCadFileActivity::class.java)
                    intent.putExtra("cadfile", Gson().toJson(boardModel?.get(position)))
                    startActivity(intent)
                    dismiss()
                })
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


    private fun startDetail(model: String) {
        val intent = Intent(this, BoardFileManagerDetailActivity::class.java)
        intent.putExtra("cadFileID", model)
        startActivity(intent)
    }

    fun getReconstruction(filter: String) {
        MyUtils.showProgress(this, flProgress)
        var id = ""
        if ((application as LidarApp).prefManager?.getOrganizationRole() == "admin") {
            id = (application as LidarApp).prefManager?.getOrganizationId().toString()
        }
        APIOpenAirUtils.getReconstruction((application as LidarApp).prefManager!!.getToken(), null, null,
            null, null, filter, id, null, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    val data = result as ReconstructionResponse
                    if (!isFirst) {
                        isFirst = true
                    }
                    data.body?.let {
                        total = data.body?.count!!
                        recentFileModel?.addAll(data.body?.rows!!)
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerActivity, flProgress)
                    MyUtils.toastError(this@BoardFileManagerActivity, error as ErrorModel)
                }
            })
    }

    fun getCadFile(pageIndex: Int,pageSize: Int, search: String?, isLoading:Boolean, isClear: Boolean) {
        if (isLoading) {
            MyUtils.showProgress(this, flProgress)
        }
        APIOpenAirUtils.getCadFile((application as LidarApp).prefManager!!.getToken(), pageIndex, pageSize,
            boardId, search, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    if (isLoading) {
                        MyUtils.hideProgress(this@BoardFileManagerActivity, flProgress)
                    }
                    val data = result as CadFileResponse
                    if (isClear) {
                        boardModel?.let {
                            it.clear()
                        }

//                        recentFile?.let {
//                            it.clear()
//                        }
                    }
                    data.response?.let {
                        totalCad = it.count!!

                        it.rows?.let {
                            if (it.isNotEmpty()) {
                                boardModel?.addAll(it)
//                                recentFile?.addAll(it)
                            }
                        }
                    }

                    boardAdapter?.notifyDataSetChanged()
//                    recentFileAdapter?.notifyDataSetChanged()
                    if (!isFirstCad) {
                        boardModel?.let {
                            if (it.isNotEmpty()) {
                                grvBroad.visibility = View.VISIBLE
                                tvEmpty.visibility = View.INVISIBLE
                            } else {
                                grvBroad.visibility = View.INVISIBLE
                                tvEmpty.visibility = View.VISIBLE
                            }
                        }
//                        recentFile?.let {
//                            if (it.isNotEmpty()) {
//                                grvRecentFile.visibility = View.VISIBLE
//                                tvEmptyRecentFile.visibility = View.INVISIBLE
//                            } else {
//                                grvRecentFile.visibility = View.GONE
//                                tvEmptyRecentFile.visibility = View.VISIBLE
//                            }
//                        }
                    }

                    boardModel?.forEachIndexed { index, board ->
                        getTotalComment(index, board.cadFileID.toString())
                    }
                }

                override fun onError(error: Any?) {
                    if (isLoading) {
                        MyUtils.hideProgress(this@BoardFileManagerActivity, flProgress)
                    }
                    MyUtils.toastError(this@BoardFileManagerActivity, error as ErrorModel)
                }
            })
    }

    fun getTotalComment(i: Int, id: String) {
        APIOpenAirUtils.getCadFileDetail((application as LidarApp).prefManager!!.getToken(), id, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                val data = result as CadFileDetailResponse
                indexLoad++
                boardModel?.let {
                    if (it.isNotEmpty()) {
                        data.response?.let {
                            it.comments?.let {
                                try {
                                    boardModel?.get(i)?.totalComment = it.size
                                } catch (e: IndexOutOfBoundsException) {
                                    e.printStackTrace()
                                }
                            }
                        }

                    }
                    if (indexLoad == it.size/2) {
                        isFirstCad = true
                    }
                    if (indexLoad == it.size) {
                        boardAdapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onError(error: Any?) {
                indexLoad++
            }
        })
    }

    fun deleteFiles(id: String) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.deleteFile((application as LidarApp).prefManager!!.getToken(), id, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                index++
                if (index == listModels?.size) {
                    listModels?.let {
                        it.clear()
                    }
                    MyUtils.hideProgress(this@BoardFileManagerActivity, flProgress)
                    lineDelete.visibility = View.GONE
//                    icOptions.visibility = View.VISIBLE
                    lineOptions.visibility = View.VISIBLE
                    boardAdapter?.let {
                        it.isRemove = false
                        it.notifyDataSetChanged()
                    }
                    resetBoard(true)
                }
            }

            override fun onError(error: Any?) {
                index++
                if (index == listModels?.size) {

                }
                MyUtils.hideProgress(this@BoardFileManagerActivity, flProgress)
                MyUtils.toastError(this@BoardFileManagerActivity, error as ErrorModel)
            }
        })
    }
}