package com.example.healthhand.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.healthhand.R

class DashBoardActivity : BaseActivity() {

    private var userId: Long = -1L
    private var userName: String = ""
    private var userEmail: String = ""


    private lateinit var head: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dash_board)

        setupBottomNav(NavTab.HOME);

        userId = intent.getLongExtra("USER_ID", -1L)
        userName = intent.getStringExtra("USER_NAME") ?: ""
        userEmail = intent.getStringExtra("USER_EMAIL;") ?: ""




        click(R.id.navBookings, MyBookingsActivity::class.java)
        click(R.id.navReports, ReportsActivity::class.java)
        click(R.id.actionBookTest, SearchTestsActivity::class.java)
        click(R.id.actionMyBookings, MyBookingsActivity::class.java)
        click(R.id.actionReports, ReportsActivity::class.java)
    }

    private fun click(viewId: Int, target: Class<*>) {
        findViewById<View>(viewId).setOnClickListener {
            startActivity(Intent(this, target).apply {
                putExtra("USER_ID", userId)
                putExtra("USER_NAME", userName)
                putExtra("USER_EMAIL", userEmail)
            })
        }
    }
}