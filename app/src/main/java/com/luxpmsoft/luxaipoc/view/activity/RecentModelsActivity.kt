package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.Select3DAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.recentmodel.ReconstructionResponse
import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_recent_models.*

class RecentModelsActivity: AppCompatActivity(), Select3DAdapter.OnListener {
    var select3DAdapter: Select3DAdapter? = null
    var model3D: ArrayList<Rows>? = ArrayList()
    var isFirst = false
    var pageIndex = 0
    var pageSize = 15
    var total = 0
    var type = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_models)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        Utils.gridLayoutManager(this, grvModel, 1, GridLayoutManager.VERTICAL)
        select3DAdapter = Select3DAdapter(this, R.layout.item_select_option_ar, model3D!!, this)
        grvModel.adapter = select3DAdapter
    }

    override fun onResume() {
        super.onResume()
        type = "All"
        clearData()
        pageIndex = 0
        isFirst = false
        getReconstruction(pageIndex, pageSize, type)
    }

    fun listener() {
        tvViewAll.setOnClickListener {
            clearData()
            pageIndex = 0
            isFirst = false
            type = "All"
            getReconstruction(pageIndex, pageSize, type)
        }

        tvObject.setOnClickListener {
            clearData()
            pageIndex = 0
            isFirst = false
            type = "Objects"
            getReconstruction(pageIndex, pageSize, type)
        }

        tvScene.setOnClickListener {
            clearData()
            pageIndex = 0
            isFirst = false
            type = "Scenes"
            getReconstruction(pageIndex, pageSize, type)
        }

        icBack.setOnClickListener {
            finish()
        }

        grvModel.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(isFirst && model3D?.size!! < total && !recyclerView.canScrollVertically(1)) {
                    pageIndex++
                    getReconstruction(pageIndex, pageSize, type)
                }
            }
        })
    }

    fun distinguish(type: String?) {
        when (type) {
            "All" -> {
                tvViewAll.background = resources.getDrawable(R.drawable.bg_choose_24)
                tvObject.background = resources.getDrawable(R.drawable.bg_grey_24)
                tvScene.background = resources.getDrawable(R.drawable.bg_grey_24)
            }
            "Objects" -> {
                tvViewAll.background = resources.getDrawable(R.drawable.bg_grey_24)
                tvObject.background = resources.getDrawable(R.drawable.bg_choose_24)
                tvScene.background = resources.getDrawable(R.drawable.bg_grey_24)
            }
            "Scenes" -> {
                tvViewAll.background = resources.getDrawable(R.drawable.bg_grey_24)
                tvObject.background = resources.getDrawable(R.drawable.bg_grey_24)
                tvScene.background = resources.getDrawable(R.drawable.bg_choose_24)
            }
        }
    }

    fun clearData() {
        model3D?.clear()
        select3DAdapter?.notifyDataSetChanged()
    }

    override fun onListener(model: String, position: Int) {
//        APIUtils.viewModels(model, object : APIInterface.onDelegate {
//            override fun onSuccess(result: Any?) {
//                MyUtils.hideProgress(this@RecentModelsActivity, flProgress)
//                val data = result as ResponseBody
//                if(data != null) {
//                    val intent = Intent(this@RecentModelsActivity, LoadHtmlActivity::class.java)
//                    intent.putExtra("url", data.string())
//                    intent.putExtra("name", model)
//                    intent.putExtra("ar", "")
//                    startActivity(intent)
//                }
//            }
//
//            override fun onError() {
//                MyUtils.hideProgress(this@RecentModelsActivity, flProgress)
//                Toast.makeText(
//                    this@RecentModelsActivity,
//                    "Error",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
        val row = Gson().toJson(model3D?.get(position))
        val intent = Intent(this@RecentModelsActivity, ShowObjectOrganizationActivity::class.java)
        intent.putExtra("data", row)
        startActivity(intent)
    }

    fun getReconstruction(pageIndex: Int,pageSize: Int, filter: String) {
        distinguish(type)
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getReconstruction((application as LidarApp).prefManager!!.getToken(), pageIndex, pageSize,
            null, null, filter, null, null, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@RecentModelsActivity, flProgress)
                val data = result as ReconstructionResponse
                if (!isFirst) {
                    isFirst = true
                }
                data.body?.let {
                    it.count?.let {
                        total = it
                    }
                    it.rows?.let {
                        model3D?.addAll(it)
                    }
                }

                select3DAdapter?.notifyDataSetChanged()

                model3D?.also {
                    if (it.size > 0) {
                        grvModel.visibility = View.VISIBLE
                        tvEmpty.visibility = View.GONE
                    } else {
                        grvModel.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@RecentModelsActivity, flProgress)
                MyUtils.toastError(this@RecentModelsActivity, error as ErrorModel)
            }
        })
    }
}