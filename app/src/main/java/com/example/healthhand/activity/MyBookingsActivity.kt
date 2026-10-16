package com.example.healthhand.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthhand.R
import com.example.healthhand.adapter.BookAdapter
import com.example.healthhand.database.SupabaseClient
import com.example.healthhand.model.Booking
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyBookingsActivity : BaseActivity() {

    private var all : List<Booking> = emptyList();

    private var current = "Upcoming"

    private val adapter = BookAdapter(emptyList()){booking ->
        Toast.makeText(this, "${booking.testName} - ${booking.status}", Toast.LENGTH_SHORT).show()
    }



    private lateinit var tvEmpty: TextView
    private lateinit var tabUpcoming: TextView
    private lateinit var tabPast: TextView
    private lateinit var tabCancelled: TextView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_bookings)
        setupBottomNav(NavTab.BOOKINGS);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rv= findViewById<RecyclerView>(R.id.bookingsList);
        rv.layoutManager= LinearLayoutManager(this)
        rv.adapter= adapter




        tvEmpty = findViewById(R.id.tvEmpty)
        tabUpcoming = findViewById(R.id.tabUpcoming)
        tabPast = findViewById(R.id.tabPast)
        tabCancelled = findViewById(R.id.tabCancelled)



        tabUpcoming.setOnClickListener { current = "Upcoming"; render() }
        tabPast.setOnClickListener { current = "Past"; render() }
        tabCancelled.setOnClickListener { current = "Cancelled"; render() }

    }

    override fun onResume() {
        super.onResume()
        loadBookings()
    }

    private fun  loadBookings(){
        lifecycleScope.launch {

            try {
                val uid = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch
                all = SupabaseClient.client.from("bookings")
                    .select {
                        filter { eq("user_id", uid) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Booking>()
                render()

            }catch (e: Exception){
                tvEmpty.text = "Could not load bookings"
                tvEmpty.visibility = View.VISIBLE

            }

        }
    }


    private fun categoryOf(b: Booking, today: String): String = when {
        b.status == "Cancelled" -> "Cancelled"
        b.status == "Completed" || b.date < today -> "Past"
        else -> "Upcoming"
    }


    private fun render() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val shown = all.filter { categoryOf(it, today) == current }
        adapter.submit(shown)

        tvEmpty.text = "No bookings here"
        tvEmpty.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE

        styleTab(tabUpcoming, current == "Upcoming")
        styleTab(tabPast, current == "Past")
        styleTab(tabCancelled, current == "Cancelled")
    }

    private fun styleTab(tab: TextView, selected: Boolean) {
        tab.setBackgroundResource(
            if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
        )
        tab.setTextColor(if (selected) Color.WHITE else 0xFF1F2937.toInt())
    }




}