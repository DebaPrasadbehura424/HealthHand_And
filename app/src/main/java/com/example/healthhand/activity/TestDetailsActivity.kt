package com.example.healthhand.activity

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.healthhand.R
import com.example.healthhand.model.Booking
import kotlinx.serialization.json.Json

class TestDetailsActivity : AppCompatActivity() {

    private lateinit var item: TestItem
    private var userId = ""
    private var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val json = intent.getStringExtra("TEST_ITEM")
        if (json == null) {
            Toast.makeText(this, "Test not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        item = try {
            Json.decodeFromString<TestItem>(json)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not read test", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        userId = intent.getStringExtra("USER_ID").orEmpty()
        userName = intent.getStringExtra("USER_NAME").orEmpty()

        bind()

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.ivShare).setOnClickListener { share() }
        findViewById<View>(R.id.btnBookNow).setOnClickListener {
            val intent = Intent(this , BookAppointmentActivity ::class.java).apply {
                putExtra("TEST_ITEM", Json.encodeToString(item))
                putExtra("USER_ID", this@TestDetailsActivity.intent.getStringExtra("USER_ID"))
                putExtra("USER_NAME", this@TestDetailsActivity.intent.getStringExtra("USER_NAME"))
            };
            startActivity(intent)
            Toast.makeText(this, "Booking: ${item.test.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bind() {
        val t = item.test

        findViewById<TextView>(R.id.tvTestName).text = t.name
        findViewById<TextView>(R.id.tvCategory).text = t.testType
        findViewById<TextView>(R.id.tvParams).text = "• ${t.parametersCount} parameters"
        findViewById<TextView>(R.id.tvPrice).text = "₹${t.price.toInt()}"
        findViewById<View>(R.id.tvPopular).visibility =
            if (t.isPopular) View.VISIBLE else View.GONE

        findViewById<ImageView>(R.id.ivHeroIcon).setImageResource(iconFor(t.testType))

        findViewById<TextView>(R.id.tvClinicName).text = item.clinicName
        findViewById<TextView>(R.id.tvClinicMeta).text =
            if (item.clinicRating > 0) "★ ${item.clinicRating} (${item.clinicReviews} reviews)"
            else "No ratings yet"

        bindIncluded(t.included)
    }

    private fun bindIncluded(list: List<String>) {
        val container = findViewById<LinearLayout>(R.id.includedList)
        container.removeAllViews()

        val rows = list.ifEmpty { listOf("Details not available") }
        rows.forEachIndexed { index, text ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = if (index == 0) 0 else dp(10) }
            }
            val icon = ImageView(this).apply {
                setImageResource(R.drawable.ic_verified)
                layoutParams = LinearLayout.LayoutParams(dp(18), dp(18))
            }
            val label = TextView(this).apply {
                this.text = text
                textSize = 14f
                setTextColor(0xFF1F2937.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = dp(10) }
            }
            row.addView(icon)
            row.addView(label)
            container.addView(row)
        }
    }

    private fun share() {
        val t = item.test
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${t.name} at ${item.clinicName} - ₹${t.price.toInt()}")
        }
        startActivity(Intent.createChooser(send, "Share test"))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun iconFor(type: String): Int = when (type) {
        "Blood" -> R.drawable.ic_test_blood
        "Urine" -> R.drawable.ic_test_urine
        "Stool" -> R.drawable.ic_test_stool
        "Thyroid" -> R.drawable.ic_test_thyroid
        else -> R.drawable.ic_help



    }
}