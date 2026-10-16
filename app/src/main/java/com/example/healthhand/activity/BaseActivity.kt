package com.example.healthhand.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.widget.ImageViewCompat
import com.example.healthhand.R

enum class NavTab { HOME, BOOKINGS, REPORTS, PROFILE }

open class BaseActivity : AppCompatActivity() {

    protected fun setupBottomNav(current: NavTab) {
        bindTab(R.id.navHome, R.id.iconHome, R.id.labelHome,
            NavTab.HOME, DashBoardActivity::class.java, current)
        bindTab(R.id.navBookings, R.id.iconBookings, R.id.labelBookings,
            NavTab.BOOKINGS, MyBookingsActivity::class.java, current)
        bindTab(R.id.navReports, R.id.iconReports, R.id.labelReports,
            NavTab.REPORTS, ReportsActivity::class.java, current)
        bindTab(R.id.navProfile, R.id.iconProfile, R.id.labelProfile,
            NavTab.PROFILE, ProfileActivity::class.java, current)
    }

    private fun bindTab(
        containerId: Int, iconId: Int, labelId: Int,
        tab: NavTab, target: Class<*>, current: NavTab
    ) {
        val color: Int = if (tab == current) {
            "#0F8A5F".toColorInt()
        } else {
            "#1F2937".toColorInt()
        }

        ImageViewCompat.setImageTintList(
            findViewById<ImageView>(iconId), ColorStateList.valueOf(color)
        )
        findViewById<TextView>(labelId).setTextColor(color)

        findViewById<View>(containerId).setOnClickListener {
            if (tab == current) return@setOnClickListener
            startActivity(
                Intent(this, target)
                    .putExtras(intent)
                    .addFlags(
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                Intent.FLAG_ACTIVITY_NO_ANIMATION
                    )
            )
        }
    }
}