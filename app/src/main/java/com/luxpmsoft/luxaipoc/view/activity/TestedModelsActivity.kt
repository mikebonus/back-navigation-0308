package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.Session
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.TestedModelAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.defectdetect.APIDefectDetectUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.defect_detect.TestedModelsData
import com.luxpmsoft.luxaipoc.model.defect_detect.TestedModelsResponse
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModels
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_tested_models.*

class TestedModelsActivity: BaseActivity(), TestedModelAdapter.OnListener {
    var testedAdapter: TestedModelAdapter? = null
    var testedModels: ArrayList<TestedModelsData>? = ArrayList()
    var pageIndex = 1
    var pageSize = 15
    var total = 0
    var trainedModels: TrainedModels? = null
    var isFirst = false
    private var session: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tested_models)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        val bundle = intent.extras
        bundle?.let {
            trainedModels = Gson().fromJson(it.get("trained").toString(), TrainedModels::class.java)
        }

        Utils.gridLayoutManager(this, grvTestModel, 1, GridLayoutManager.VERTICAL)
        testedAdapter = TestedModelAdapter(this, R.layout.item_tested_models, testedModels!!, this)
        grvTestModel.adapter = testedAdapter
        getTestedModel(pageIndex, pageSize, (application as LidarApp).prefManager!!.getUserId(), trainedModels?.sessionId)
        session = MyUtils.createSession(this)
    }

    override fun onResume() {
        super.onResume()
        footer.updateTabRecent((application as LidarApp).prefManager?.getTotalNotification()!!)
        footer.updateSession(session)
    }

    fun listener() {
        grvTestModel.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(isFirst && testedModels?.size!! < total && !recyclerView.canScrollVertically(1)) {
                    pageIndex++
                    getTestedModel(pageIndex, pageSize, (application as LidarApp).prefManager!!.getUserId(), trainedModels?.sessionId)
                }
            }
        })

        icBack.setOnClickListener {
            finish()
        }

        tvTestModel.setOnClickListener {
            startRecording()
        }

        btStartTest.setOnClickListener {
            startRecording()
        }
    }

    fun startRecording() {
        val intent = Intent(this, DefectDetectionRecordingActivity::class.java)
        intent.putExtra("trained", Gson().toJson(trainedModels))
        startActivity(intent)
    }

    fun getTestedModel(pageIndex: Int,pageSize: Int,userId: String?, session_id: String?) {
        MyUtils.showProgress(this, flProgress)
        APIDefectDetectUtils.getTestedModel((application as LidarApp).prefManager!!.getToken(), userId!!, session_id!!,  pageIndex, pageSize, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@TestedModelsActivity, flProgress)
                val data = result as TestedModelsResponse
                if (!isFirst) {
                    isFirst = true
                }

                data.total_data_count?.let {
                    total = it
                }
                data.data?.let {
                    if (it.isNotEmpty()) {
                        testedModels?.addAll(it)
                        testedAdapter?.notifyDataSetChanged()
                    }

                    if (testedModels!!.isNotEmpty()) {
                        grvTestModel.visibility = View.VISIBLE
                        lineEmpty.visibility = View.GONE
                        tvTestModel.visibility = View.VISIBLE
                    } else {
                        grvTestModel.visibility = View.GONE
                        lineEmpty.visibility = View.VISIBLE
                        tvTestModel.visibility = View.GONE
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@TestedModelsActivity, flProgress)
                MyUtils.toastError(this@TestedModelsActivity, error as ErrorModel)
            }
        })
    }

    override fun onListener(position: Int) {
        val intent = Intent(this, TestedFramesActivity::class.java)
        intent.putExtra("frames", Gson().toJson(testedModels!!.get(position)))
        intent.putExtra("trained", Gson().toJson(trainedModels))
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        session?.let {
            session = null
        }
    }
}