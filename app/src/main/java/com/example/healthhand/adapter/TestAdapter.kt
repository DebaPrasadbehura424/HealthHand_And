package com.example.healthhand.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthhand.R
import com.example.healthhand.model.Test

data class TestItem(
    val test: Test,
    val clinicName: String,
    val clinicRating: Double = 0.0,
    val clinicReviews: Int = 0
)

class TestAdapter(
    private var items: List<TestItem>,
    private val onClick: (TestItem) -> Unit
) : RecyclerView.Adapter<TestAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.ivTestIcon)
        val name: TextView = v.findViewById(R.id.tvTestName)
        val price: TextView = v.findViewById(R.id.tvPrice)
        val meta: TextView = v.findViewById(R.id.tvMeta)
        val desc: TextView = v.findViewById(R.id.tvDescription)
        val clinic: TextView = v.findViewById(R.id.tvClinicName)
        val rating: TextView = v.findViewById(R.id.tvRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_test, parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        val t = item.test

        h.icon.setImageResource(iconFor(t.testType))
        h.name.text = t.name
        h.price.text = "₹${t.price.toInt()}"
        h.meta.text = "${t.testType} • ${t.parametersCount} parameters"
        h.desc.text = t.description
        h.desc.visibility = if (t.description.isBlank()) View.GONE else View.VISIBLE
        h.clinic.text = item.clinicName

        if (item.clinicRating > 0) {
            h.rating.text = "★ ${item.clinicRating} (${item.clinicReviews})"
            h.rating.visibility = View.VISIBLE
        } else {
            h.rating.visibility = View.GONE
        }

        h.itemView.setOnClickListener { onClick(item) }
    }

    private fun iconFor(type: String): Int = when (type) {
        "Blood" -> R.drawable.ic_test_blood
        "Urine" -> R.drawable.ic_test_urine
        "Stool" -> R.drawable.ic_test_stool
        "Throid" -> R.drawable.ic_test_thyroid
        else -> R.drawable.ic_help
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<TestItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}