package com.example.healthhand.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthhand.R
import com.example.healthhand.database.SupabaseClient
import com.example.healthhand.model.Clinic
import com.example.healthhand.model.Test
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SearchTestsActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var tvCount: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var rvTests: RecyclerView
    private lateinit var popularGroup: ChipGroup
    private lateinit var adapter: TestAdapter

    private var allItems: List<TestItem> = emptyList()
    private var selectedType = "All"
    private var query = ""

    private val chips by lazy {
        mapOf(
            "All" to findViewById<TextView>(R.id.chipAll),
            "Blood" to findViewById<TextView>(R.id.chipBlood),
            "Urine" to findViewById<TextView>(R.id.chipUrine),
            "Stool" to findViewById<TextView>(R.id.chipStool),
            "Other" to findViewById<TextView>(R.id.chipOther)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_tests)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        etSearch = findViewById(R.id.etSearch)
        tvCount = findViewById(R.id.tvCount)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvTests = findViewById(R.id.rvTests)
        popularGroup = findViewById(R.id.popularGroup)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        adapter = TestAdapter(emptyList()) { item ->
            Toast.makeText(this, "${item.test.name} @ ${item.clinicName}", Toast.LENGTH_SHORT).show()
        }
        rvTests.layoutManager = LinearLayoutManager(this)
        rvTests.adapter = adapter

        chips.forEach { (type, view) ->
            view.setOnClickListener {
                selectedType = type
                applyFilter()
            }
        }
        etSearch.doAfterTextChanged {
            query = it?.toString()?.trim().orEmpty()
            applyFilter()
        }

        applyFilter()   // chips ka initial highlight (All selected)
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val clinicsDef = async { SupabaseClient.client.from("clinics").select().decodeList<Clinic>() }
                val testsDef = async { SupabaseClient.client.from("tests").select().decodeList<Test>() }

                val clinics = clinicsDef.await()
                val tests = testsDef.await()

                val clinicById = clinics.associateBy { it.id }
                allItems = tests.map { t ->
                    val c = clinicById[t.clinicId]
                    TestItem(
                        test = t,
                        clinicName = c?.name ?: "",
                        clinicRating = c?.rating ?: 0.0,
                        clinicReviews = c?.reviewsCount ?: 0
                    )
                }

                buildPopularChips()
                applyFilter()
            } catch (e: Exception) {
                Log.e("SEARCH", "load failed", e)
                tvEmpty.text = "Could not load tests"
                tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun buildPopularChips() {
        popularGroup.removeAllViews()
        allItems.filter { it.test.isPopular }
            .distinctBy { it.test.name }
            .forEach { item ->
                popularGroup.addView(Chip(this).apply {
                    text = item.test.name
                    setOnClickListener { etSearch.setText(item.test.name) }
                })
            }
    }

    private fun applyFilter() {
        val result = allItems.filter { item ->
            val typeOk = selectedType == "All" || item.test.testType == selectedType
            val queryOk = query.isEmpty() ||
                    item.test.name.contains(query, ignoreCase = true) ||
                    item.test.description.contains(query, ignoreCase = true) ||
                    item.clinicName.contains(query, ignoreCase = true)
            typeOk && queryOk
        }

        adapter.submit(result)
        tvCount.text = "${result.size} tests"
        tvEmpty.text = "No tests found"
        tvEmpty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE

        chips.forEach { (type, view) ->
            val selected = type == selectedType
            view.setBackgroundResource(
                if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
            )
            view.setTextColor(if (selected) 0xFFFFFFFF.toInt() else 0xFF1F2937.toInt())
        }
    }
}