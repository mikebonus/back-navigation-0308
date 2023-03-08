package com.luxpmsoft.luxaipoc.view.activity.login.welcome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.databinding.ActivityWelcomeOpenAirBinding
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.add_organization.AddOrganizationActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.organization.JoinOrganizationActivity

class WelcomeOpenAirActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeOpenAirBinding

    private val adapter by lazy { WelcomeOpenAirAdapter(::onItemClicked) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        binding = ActivityWelcomeOpenAirBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setButtonContent()
        setListeners()
    }

    private fun setButtonContent() {
        binding.viewAddOrganization.text.text = getString(R.string.common_add_organization)
        binding.viewAddOrganization.image.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_add
            )
        )
        binding.viewJoinOrganization.text.text = getString(R.string.common_join_organization)
        binding.viewJoinOrganization.image.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_join_organization
            )
        )
    }

    private fun setupRecyclerView() {
        adapter.setData(listOf())
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun setListeners() {
        binding.viewAddOrganization.background.setOnClickListener { startAddOrganizationActivity() }
        binding.viewJoinOrganization.background.setOnClickListener { startJoinOrganizationActivity() }
    }

    private fun startAddOrganizationActivity() {
        val intent = Intent(this, AddOrganizationActivity::class.java)
        startActivity(intent)
    }

    private fun startJoinOrganizationActivity() {
        val intent = Intent(this, JoinOrganizationActivity::class.java)
        startActivity(intent)
    }

    private fun onItemClicked(intent: String) {}
}
