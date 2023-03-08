package com.luxpmsoft.luxaipoc.view.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import com.luxpmsoft.luxaipoc.widget.TranslucentDialog
import kotlinx.android.synthetic.main.activity_base.*

open class BaseActivity: AppCompatActivity(){
    var baseLayout: FrameLayout? = null
    val SPLASH_TIME_OUT = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    override fun onResume() {
        super.onResume()
    }

//    override fun setContentView(layoutResID: Int) {
////        super.setContentView(layoutResID)
//        baseLayout = layoutInflater.inflate(R.layout.activity_base, null) as FrameLayout?
//        layoutInflater.inflate(layoutResID, content_layout, true)
//        super.setContentView(baseLayout)
//    }

    open fun transitionAnimation() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    fun showProgressBar() {
        runOnUiThread { cover_layout_common.visibility = View.VISIBLE }
    }

    fun hideProgressBar() {
        runOnUiThread { cover_layout_common.visibility = View.GONE }
    }

    open fun CheckNetworkRequest(baseActivity: BaseActivity?, listener: CheckNetworkListener) {
        try {
            if (MyUtils.isNetworkAvailable(applicationContext)) {
                listener.Connected()
            } else {
                CheckNetworkRequestAgain(getString(R.string.checkInternet), baseActivity!!, listener)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //-------- Check Network Request ----------
    fun CheckNetworkRequestAgain(
        errorMessage: String,
        baseActivity: BaseActivity,
        listener: CheckNetworkListener
    ) {
        try {
            DialogFactory.createTryNetAgainDialog(
                baseActivity,
                errorMessage,
                object : DialogFactory.Companion.DialogListener.RetryNetListener {
                    override fun retry(dialog: TranslucentDialog?) {
                        if (MyUtils.isNetworkAvailable(applicationContext)) {
                            listener.Connected()
                            dialog!!.dismiss()
                        } else {
                            dialog!!.dismiss()
                            baseActivity.showProgressBar()
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed({
                                baseActivity.hideProgressBar()
                                CheckNetworkRequestAgain(
                                    getString(R.string.checkInternet),
                                    baseActivity,
                                    listener
                                )
                            }, SPLASH_TIME_OUT.toLong())
                        }
                    }
                })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    interface CheckNetworkListener {
        fun Connected()
    }
}