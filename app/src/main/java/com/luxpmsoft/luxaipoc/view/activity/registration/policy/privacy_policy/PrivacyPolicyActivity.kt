package com.luxpmsoft.luxaipoc.view.activity.registration.policy.privacy_policy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.databinding.ActivityPrivacyPolicyBinding
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.registration.policy.PolicyAdapter

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    private val adapter by lazy { PolicyAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        MyUtils.setStatusBarTransparentFlagBlack(this)

        setContent()
        setListeners()
    }

    private fun setContent() {
        binding.topBar.textTitle.text = getString(R.string.policy_screen_privacy_policy)
    }

    private fun setListeners() {
        binding.topBar.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter.setData(listOf())
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}
