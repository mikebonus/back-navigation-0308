package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.TrainedModelAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.defectdetect.APIDefectDetectUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModelResponse
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModels
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_defect_detection_models.*

class DefectDetectionModelsActivity : BaseActivity(), TrainedModelAdapter.OnListener {
    var trainedAdapter: TrainedModelAdapter? = null
    var trainedModels: ArrayList<TrainedModels>? = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        setContentView(R.layout.activity_defect_detection_models)
        init()
        listener()
    }

    override fun onResume() {
        super.onResume()
        footer.updateTabHome((applicationContext as LidarApp).prefManager?.getTotalNotification()!!)
    }

    fun init() {
        Utils.gridLayoutManager(this, grvTrainedModels, 1, GridLayoutManager.VERTICAL)
        trainedAdapter = TrainedModelAdapter(this, R.layout.item_trained_models, trainedModels!!, this)
        grvTrainedModels.adapter = trainedAdapter
        getTrainedModels(1, 100, (application as LidarApp).prefManager!!.getUserId())
    }

    fun listener() {
        icBack.setOnClickListener {
            finish()
        }
    }

    override fun onListener(position: Int) {
        val intent = Intent(this, TestedModelsActivity::class.java)
        intent.putExtra("trained", Gson().toJson(trainedModels?.get(position)))
        startActivity(intent)
    }

    fun getTrainedModels(pageIndex: Int,pageSize: Int,userId: String?) {
        MyUtils.showProgress(this, flProgress)
        APIDefectDetectUtils.getTrainedModels((application as LidarApp).prefManager!!.getToken(),
            userId!!, pageIndex, pageSize, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@DefectDetectionModelsActivity, flProgress)
                    val data = result as TrainedModelResponse
                    data.data?.let {
                        if (it.isNotEmpty()) {
                            trainedModels?.addAll(it)
                            trainedAdapter?.notifyDataSetChanged()
                            grvTrainedModels.visibility = View.VISIBLE
                            tvEmpty.visibility = View.GONE
                        }
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@DefectDetectionModelsActivity, flProgress)
                    MyUtils.toastError(this@DefectDetectionModelsActivity, error as ErrorModel)
                }
            })
    }

}