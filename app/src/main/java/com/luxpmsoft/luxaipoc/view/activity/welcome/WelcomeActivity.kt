package com.luxpmsoft.luxaipoc.view.activity.welcome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.WelcomeSlide
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.view.activity.BaseActivity
import com.luxpmsoft.luxaipoc.view.activity.login.SignInActivity
import com.luxpmsoft.luxaipoc.view.activity.registration.SignUpActivity
import kotlinx.android.synthetic.main.activity_welcome1.*

class WelcomeActivity : BaseActivity() {

    private val adapter by lazy { WelcomeSliderAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_welcome1)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        setListeners()
        setupViewpager()
    }

    private fun setupViewpager() {
        adapter.setData(setupSlides())
        view_pager.adapter = adapter
        view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                updateCircleMarker(position)
            }
        })
    }

    private fun setListeners() {
        button_sign_up.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        button_log_in.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateCircleMarker(position: Int) {
        when (position) {
            0 -> {
                image_first_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_active
                    )
                )
                image_second_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
                image_third_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
                image_fourth_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
            }
            1 -> {
                image_first_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
                image_second_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_active
                    )
                )
                image_third_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
                image_fourth_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
            }
            2 -> {
                image_first_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
                image_second_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
                image_third_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_active
                    )
                )
                image_fourth_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
            }
            3 -> {
                image_first_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
                image_second_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
                image_third_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_inactive
                    )
                )
                image_fourth_indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_indicator_active
                    )
                )
            }
        }
    }

    private fun setupSlides(): List<WelcomeSlide> = listOf(
        WelcomeSlide(
            title = getString(R.string.welcome_screen_title),
            subtitle = getString(R.string.welcome_screen_subtitle),
            image = R.drawable.ic_luxolis_logo,
        ),
        WelcomeSlide(
            title = getString(R.string.welcome_screen_title_step_1),
            subtitle = getString(R.string.welcome_screen_subtitle_step_1),
            image = R.drawable.ic_welcome_screen_image_step_1,
        ),
        WelcomeSlide(
            title = getString(R.string.welcome_screen_title_step_2),
            subtitle = getString(R.string.welcome_screen_subtitle_step_2),
            image = R.drawable.ic_welcome_screen_image_step_2,
        ),
        WelcomeSlide(
            title = getString(R.string.welcome_screen_title_step_3),
            subtitle = getString(R.string.welcome_screen_subtitle_step_3),
            image = R.drawable.ic_welcome_screen_image_step_3,
        ),
    )
}
