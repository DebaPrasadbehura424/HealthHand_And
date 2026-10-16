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
import com.example.healthhand.model.Profile
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        initViews()

        btnLogin.setOnClickListener {
            if (validateInputs()) {
                loginUser()
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
    }

    private fun validateInputs(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        tilEmail.error = null
        tilPassword.error = null

        var isValid = true

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Enter a valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            tilPassword.error = "Enter your password"
            isValid = false
        }

        return isValid
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()


        print(email)
        print(password)
        lifecycleScope.launch {
            setLoading(true)

            try {
                // Move network work off the main thread
               val profile= withContext(Dispatchers.IO) {
                    SupabaseClient.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
//                   val userId = SupabaseClient.client.auth.currentUserOrNull()?.id  ?:throw IllegalStateException("User Not Found")

                   SupabaseClient.client.from("profiles")
                       .select { filter { eq("email", email) } }
                       .decodeSingleOrNull<Profile>()
                       ?: throw IllegalStateException("Profile not found")
                }

                val intent = Intent(this@LoginActivity, DashBoardActivity::class.java).apply {
                    putExtra("USER_ID", profile.id)
                    putExtra("USER_NAME", profile.name)
                    putExtra("USER_EMAIL", profile.email)
                }

                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                startActivity(Intent(intent))
                finish()

            } catch (e: RestException) {
                Log.e("AUTH", "Supabase error", e)
                Toast.makeText(
                    this@LoginActivity,   // use this@LoginActivity in LoginActivity
                    "${e.statusCode}: ${e.error} - ${e.description}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("AUTH", "Other error", e)
                Toast.makeText(
                    this@LoginActivity,
                    e.message?.lineSequence()?.firstOrNull() ?: "Something went wrong",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        btnLogin.isEnabled = !loading
        tvRegister.isEnabled = !loading
        btnLogin.text = if (loading) "Signing in..." else "Login"
    }
}