package com.example.healthhand.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.healthhand.R
import com.example.healthhand.database.SupabaseClient
import com.example.healthhand.model.Booking
import com.example.healthhand.model.Profile
import com.example.healthhand.model.Test
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var item: TestItem
    private var userName = ""

    private var collectionType = HOME
    private var dateIso: String? = null
    private var timeSlot: String? = null
    private var address = ""

    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvAddress: TextView
    private lateinit var etNotes: EditText
    private lateinit var btnConfirm: TextView
    private lateinit var root: View

    private val slots = arrayOf(
        "07:00 AM - 09:00 AM", "09:00 AM - 11:00 AM", "11:00 AM - 01:00 PM",
        "02:00 PM - 04:00 PM", "04:00 PM - 06:00 PM", "06:00 PM - 08:00 PM"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_appointment)
        root = findViewById(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val json = intent.getStringExtra("TEST_ITEM")
        if (json == null) { finish(); return }
        item = try {
            Json.decodeFromString<TestItem>(json)
        } catch (e: Exception) { finish(); return }
        userName = intent.getStringExtra("USER_NAME").orEmpty()

        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        tvAddress = findViewById(R.id.tvAddress)
        etNotes = findViewById(R.id.etNotes)
        btnConfirm = findViewById(R.id.btnConfirm)

        // summary
        findViewById<TextView>(R.id.tvTestName).text = item.test.name
        findViewById<TextView>(R.id.tvTestMeta).text =
            "${item.test.testType} • ₹${item.test.price.toInt()}"
        findViewById<TextView>(R.id.tvClinicName).text = item.clinicName
        findViewById<TextView>(R.id.tvClinicMeta).text =
            if (item.clinicRating > 0) "★ ${item.clinicRating} (${item.clinicReviews})" else "No ratings yet"
        findViewById<TextView>(R.id.tvTotal).text = "Total: ₹${item.test.price.toInt()}"
        tvAddress.text = "Tap Change to add address"

        findViewById<View>(R.id.ivBack).setOnClickListener { finish() }
        findViewById<View>(R.id.cardHomeCollection).setOnClickListener { selectCollection(HOME) }
        findViewById<View>(R.id.cardVisitLab).setOnClickListener { selectCollection(LAB) }
        findViewById<View>(R.id.cardDate).setOnClickListener { pickDate() }
        findViewById<View>(R.id.cardTime).setOnClickListener { pickTime() }
        findViewById<View>(R.id.tvChangeAddress).setOnClickListener { editAddress() }
        btnConfirm.setOnClickListener { confirmBooking() }

        selectCollection(HOME)
    }

    private fun selectCollection(type: String) {
        collectionType = type
        val home = type == HOME
        style(R.id.cardHomeCollection, R.id.tvHomeLabel, home)
        style(R.id.cardVisitLab, R.id.tvLabLabel, !home)
        findViewById<View>(R.id.sectionAddress).visibility = if (home) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.tvCollectionNote).text =
            if (home) "Home collection is free" else "Visit ${item.clinicName} at your slot time"
    }

    private fun style(cardId: Int, labelId: Int, selected: Boolean) {
        findViewById<MaterialCardView>(cardId).apply {
            setCardBackgroundColor(if (selected) 0xFFE8F5EE.toInt() else 0xFFFFFFFF.toInt())
            strokeColor = if (selected) 0xFF0F8A5F.toInt() else 0xFFE5E7EB.toInt()
            strokeWidth = if (selected) 4 else 2
        }
        findViewById<TextView>(labelId).setTextColor(
            if (selected) 0xFF0F8A5F.toInt() else 0xFF1F2937.toInt()
        )
    }

    private fun pickDate() {
        val c = Calendar.getInstance()
        val dialog = DatePickerDialog(this, { _, y, m, d ->
            val picked = Calendar.getInstance().apply { set(y, m, d) }
            dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(picked.time)
            tvDate.text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(picked.time)
            tvDate.setTextColor(0xFF1F2937.toInt())
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        dialog.show()
    }

    private fun pickTime() {
        AlertDialog.Builder(this)
            .setTitle("Select time slot")
            .setItems(slots) { _, which ->
                timeSlot = slots[which]
                tvTime.text = slots[which]
                tvTime.setTextColor(0xFF1F2937.toInt())
            }.show()
    }

    private fun editAddress() {
        val input = EditText(this).apply {
            setText(address)
            hint = "House no, street, area, city"
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(this)
            .setTitle("Collection address")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                address = input.text.toString().trim()
                tvAddress.text = address.ifEmpty { "Tap Change to add address" }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmBooking() {
        val date = dateIso
        val slot = timeSlot
        when {
            date == null -> return showMsg("Please select a date", false)
            slot == null -> return showMsg("Please select a time slot", false)
            collectionType == HOME && address.length < 10 ->
                return showMsg("Please enter a full address", false)
        }

        lifecycleScope.launch {
            setLoading(true)
            try {
                val authUser = SupabaseClient.client.auth.currentUserOrNull()
                    ?: throw IllegalStateException("Session expired. Please login again.")

                // fresh price from DB, not from intent
                val fresh = SupabaseClient.client.from("tests")
                    .select { filter { eq("id", item.test.id) } }
                    .decodeSingle<Test>()

                val profile = SupabaseClient.client.from("profiles")
                    .select { filter { eq("id", authUser.id) } }
                    .decodeSingleOrNull<Profile>()

                SupabaseClient.client.from("bookings").insert(
                    Booking(
                        userId = authUser.id,
                        testId = fresh.id,
                        clinicId = fresh.clinicId,
                        userName = profile?.name ?: userName,
                        userPhone = profile?.phoneNumber.orEmpty(),
                        testName = fresh.name,
                        testType = fresh.testType,
                        clinicName = item.clinicName,
                        amount = fresh.price,
                        collectionType = collectionType,
                        date = date,
                        timeSlot = slot,
                        address = if (collectionType == HOME) address else "",
                        instructions = etNotes.text.toString().trim()
                    )
                )

                showMsg("Booking confirmed successfully", true)
                delay(1500.milliseconds)

                startActivity(
                    Intent(this@BookAppointmentActivity, DashBoardActivity::class.java)
                        .putExtra("USER_ID", intent.getStringExtra("USER_ID"))
                        .putExtra("USER_NAME", userName)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
                finish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: RestException) {
                showMsg(e.description ?: e.error, false)
            } catch (e: HttpRequestException) {
                showMsg("No internet connection", false)
            } catch (e: Exception) {
                showMsg(e.message?.lineSequence()?.firstOrNull() ?: "Booking failed", false)
            } finally {
                setLoading(false)
            }
        }


    }

    private fun setLoading(loading: Boolean) {
        btnConfirm.isEnabled = !loading
        btnConfirm.text = if (loading) "Booking..." else "Confirm Booking"
    }

    private fun showMsg(msg: String, success: Boolean) {
        Snackbar.make(root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(if (success) 0xFF0F8A5F.toInt() else 0xFFE53935.toInt())
            .setTextColor(0xFFFFFFFF.toInt())
            .show()
    }

    companion object {
        private const val HOME = "Home Collection"
        private const val LAB = "Visit Lab"
    }
}