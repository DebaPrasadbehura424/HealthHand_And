package com.example.healthhand.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.healthhand.R
import com.google.android.material.button.MaterialButton

class DashBoardActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dash_board)
        findViewById<View>(R.id.ivProfile).setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }


        click(R.id.ivProfile, Profile::class.java)
        click(R.id.navBookings, MyBookingsActivity::class.java)
        click(R.id.navReports, ReportsActivity::class.java)
        click(R.id.navProfile, Profile::class.java)
        click(R.id.actionBookTest, SearchTestsActivity::class.java)
        click(R.id.actionMyBookings, MyBookingsActivity::class.java)
        click(R.id.actionReports, ReportsActivity::class.java)


    }

    private fun click (viewId : Int , target :  Class<*> ){
        findViewById<View>(viewId).setOnClickListener {
            startActivity( Intent(this,target));
        }

    }





}