package com.luxpmsoft.luxaipoc.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.SelectTagAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.BaseResponse
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.recentmodel.Tags
import com.luxpmsoft.luxaipoc.utils.MyUtils
import kotlinx.android.synthetic.main.activity_select_tag.*
class SelectTagActivity : BaseActivity(), SelectTagAdapter.OnListener {

    var commentId = ""
    var tagsModel: ArrayList<Tags>? = ArrayList()
    var tagsAdapter: SelectTagAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_tag)

        init()
    }

    fun init() {

        commentId = intent.getStringExtra("commentId").toString()
        tagsModel = intent.getSerializableExtra("tagList") as ArrayList<Tags>?
        var tag = findViewById<RecyclerView>(R.id.grvTags)
        Utils.gridLayoutManager(this@SelectTagActivity, grvTags, 1, GridLayoutManager.VERTICAL)
        tagsAdapter = SelectTagAdapter(this@SelectTagActivity, R.layout.item_select_tag, tagsModel!!, this@SelectTagActivity)
        tag.adapter = tagsAdapter

        ivClose.setOnClickListener {
            finish()
        }
    }

    override fun onListener(tag: Tags) {
        val intent = Intent()
        intent.putExtra("tag", tag)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onDelete(position: Int, tagId: String, tagName: String) {
        deleteTag(tagId, position)
    }

    fun deleteTag(id: String, position: Int) {
        APIOpenAirUtils.deleteTag((application as LidarApp).prefManager!!.getToken(),
            id, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@SelectTagActivity, flProgress)
                    val data = result as BaseResponse
                    tagsModel?.removeAt(position)
                    tagsAdapter?.notifyDataSetChanged()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@SelectTagActivity, flProgress)
                    MyUtils.toastError(this@SelectTagActivity, error as ErrorModel)
                }
            })
    }
}