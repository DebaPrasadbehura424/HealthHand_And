package com.example.healthhand.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.healthhand.R
import com.example.healthhand.database.SupabaseClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText

    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        initViews()

        btnRegister.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun initViews() {
        tilName = findViewById(R.id.tilName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPhone = findViewById(R.id.tilPhone)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun validateInputs(): Boolean {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        tilName.error = null
        tilEmail.error = null
        tilPhone.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null

        var isValid = true

        if (name.length < 2) {
            tilName.error = "Enter your full name"
            isValid = false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Enter a valid email"
            isValid = false
        }

        if (phone.length < 10 || !Patterns.PHONE.matcher(phone).matches()) {
            tilPhone.error = "Enter a valid phone number"
            isValid = false
        }

        if (password.length < 8) {
            tilPassword.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (confirmPassword != password) {
            tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phoneNumber = etPhone.text.toString().trim()
        val password = etPassword.text.toString()

        lifecycleScope.launch {
            setLoading(true)

            try {

                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("name", name)
                        put("phone_number", phoneNumber)
                    }
                }

                val loggedIn = SupabaseClient.client.auth.currentUserOrNull() != null

                if (loggedIn) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration successful!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Account created! Please check your email, confirm, then login.",
                        Toast.LENGTH_LONG
                    ).show()

                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                }
                finish()

            }catch (e: RestException) {
                Log.e("AUTH", "Supabase error", e)
                Toast.makeText(
                    this@RegisterActivity,
                    "${e.statusCode}: ${e.error} - ${e.description}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("AUTH", "Other error", e)
                Toast.makeText(
                    this@RegisterActivity,
                    e.message?.lineSequence()?.firstOrNull() ?: "Something went wrong",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        btnRegister.isEnabled = !loading
        btnRegister.text = if (loading) "Creating account..." else "Register"
    }
}