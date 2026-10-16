package com.example.healthhand.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.healthhand.R
import com.example.healthhand.database.SupabaseClient
import com.example.healthhand.model.Profile
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {
    private lateinit var  tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        setupBottomNav(NavTab.PROFILE)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)

        tvName.text= intent.getStringExtra("USER_NAME");
        tvEmail.text = ""

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        findViewById<View>(R.id.rowLogout).setOnClickListener { logout() };



        loadProfile()
    }

    private fun loadProfile(){

        lifecycleScope.launch {

            val userId = intent.getStringExtra("USER_ID")?.takeIf { it.isNotEmpty() }
                ?: SupabaseClient.client.auth.currentUserOrNull()?.id
                ?: run { goToLogin(); return@launch }




            val profile =SupabaseClient.client.from("profiles")
                .select { filter { eq("id",userId) } }
                .decodeSingleOrNull<Profile>()

        }

    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }


    private fun logout() {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                Log.e("PROFILE", "signOut failed", e)
            }
            goToLogin()
        }
    }










}