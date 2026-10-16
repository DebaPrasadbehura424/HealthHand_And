package com.example.healthhand.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.healthhand.R
import com.example.healthhand.database.SupabaseClient
import com.example.healthhand.model.Profile
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private lateinit var root: View
    private lateinit var tvName: TextView
    private lateinit var tvInfoEmail: TextView
    private lateinit var tvInfoPhone: TextView
    private lateinit var etOld: EditText
    private lateinit var etNew: EditText
    private lateinit var btnUpdate: TextView

    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        root = findViewById(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        setupBottomNav(NavTab.PROFILE)

        tvName = findViewById(R.id.tvProfileName)
        tvInfoEmail = findViewById(R.id.tvInfoEmail)
        tvInfoPhone = findViewById(R.id.tvInfoPhone)
        etOld = findViewById(R.id.etOldPassword)
        etNew = findViewById(R.id.etNewPassword)
        btnUpdate = findViewById(R.id.btnUpdatePassword)

        // name turant dikhao (intent se), baaki DB se aayega
        tvName.text = intent.getStringExtra("USER_NAME").orEmpty()

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        // dropdown rows
        dropdown(R.id.rowPersonalInfo, R.id.panelPersonal, R.id.chevPersonal)
        dropdown(R.id.rowChangePassword, R.id.panelPassword, R.id.chevPassword)
        dropdown(R.id.rowHelp, R.id.panelHelp, R.id.chevHelp)
        dropdown(R.id.rowAbout, R.id.panelAbout, R.id.chevAbout)

        btnUpdate.setOnClickListener { changePassword() }
        findViewById<View>(R.id.rowLogout).setOnClickListener { confirmLogout() }

        loadProfile()
    }

    // ---------- dropdown ----------
    private fun dropdown(rowId: Int, panelId: Int, chevronId: Int) {
        val panel = findViewById<View>(panelId)
        val chevron = findViewById<View>(chevronId)
        findViewById<View>(rowId).setOnClickListener {
            TransitionManager.beginDelayedTransition(findViewById<LinearLayout>(R.id.optionsContainer))
            val open = panel.visibility != View.VISIBLE
            panel.visibility = if (open) View.VISIBLE else View.GONE
            chevron.animate().rotation(if (open) 90f else 0f).setDuration(150).start()
        }
    }

    // ---------- profile ----------
    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                // id session se, intent se nahi (RLS auth.uid() pe chalti hai)
                val authUser = SupabaseClient.client.auth.currentUserOrNull()
                    ?: run { goToLogin(); return@launch }

                userEmail = authUser.email.orEmpty()
                tvInfoEmail.text = userEmail.ifEmpty { "Not added" }

                val profile = SupabaseClient.client.from("profiles")
                    .select { filter { eq("id", authUser.id) } }
                    .decodeSingleOrNull<Profile>()

                if (profile == null) {
                    showMsg("Profile not found", false)
                    return@launch
                }

                tvName.text = profile.name.orEmpty().ifEmpty { "User" }
                tvInfoEmail.text = profile.email.orEmpty().ifEmpty { userEmail.ifEmpty { "Not added" } }
                tvInfoPhone.text = profile.phoneNumber.orEmpty().ifEmpty { "Not added" }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("PROFILE", "load failed", e)
                showMsg("Could not load profile", false)
            }
        }
    }

    // ---------- change password ----------
    private fun changePassword() {
        val oldPass = etOld.text.toString()
        val newPass = etNew.text.toString()

        when {
            oldPass.isEmpty() -> return showMsg("Enter your old password", false)
            newPass.length < 6 -> return showMsg("New password must be at least 6 characters", false)
            newPass == oldPass -> return showMsg("New password must be different", false)
        }

        lifecycleScope.launch {
            btnUpdate.isEnabled = false
            btnUpdate.text = "Updating..."
            try {
                val auth = SupabaseClient.client.auth
                val mail = auth.currentUserOrNull()?.email ?: userEmail
                if (mail.isEmpty()) throw IllegalStateException("Session expired. Please login again.")

                // 1. old password check: usi password se dobara sign in
                auth.signInWith(Email) {
                    this.email = mail
                    this.password = oldPass
                }

                // 2. new password set
                auth.updateUser { password = newPass }

                etOld.text.clear()
                etNew.text.clear()
                showMsg("Password updated successfully", true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: RestException) {
                val msg = if (e.statusCode.value == 400) "Old password is incorrect"
                else e.description ?: e.error
                showMsg(msg, false)
            } catch (e: HttpRequestException) {
                showMsg("No internet connection", false)
            } catch (e: Exception) {
                showMsg(e.message?.lineSequence()?.firstOrNull() ?: "Could not update password", false)
            } finally {
                btnUpdate.isEnabled = true
                btnUpdate.text = "Update Password"
            }
        }
    }

    // ---------- logout ----------
    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Do you really want to logout?")
            .setPositiveButton("Confirm") { _, _ -> logout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("PROFILE", "signOut failed", e)
            }
            goToLogin()
        }
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    private fun showMsg(msg: String, success: Boolean) {
        Snackbar.make(root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(if (success) 0xFF0F8A5F.toInt() else 0xFFE53935.toInt())
            .setTextColor(0xFFFFFFFF.toInt())
            .show()
    }
}