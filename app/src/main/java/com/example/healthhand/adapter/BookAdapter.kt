package com.example.healthhand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthhand.R
import com.example.healthhand.model.Booking

class BookAdapter(private var items : List<Booking>,
                  private val onClick : (Booking)-> Unit) : RecyclerView.Adapter<BookAdapter.VH>() {


    class VH (v: View) : RecyclerView.ViewHolder(v){
        val name: TextView = v.findViewById(R.id.tvBTestName)
        val amount: TextView = v.findViewById(R.id.tvBAmount)
        val clinic: TextView = v.findViewById(R.id.tvBClinic)
        val whenTv: TextView = v.findViewById(R.id.tvBWhen)
        val status: TextView = v.findViewById(R.id.tvBStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_booking,parent,false))

    override fun onBindViewHolder(h: VH, i: Int) {
        val b = items[i]
        h.name.text = b.testName
        h.amount.text = "₹${b.amount.toInt()}"
        h.clinic.text = b.clinicName
        h.whenTv.text = "${b.date} • ${b.timeSlot}"
        h.status.text = b.status
        h.itemView.setOnClickListener { onClick(b) }
    }

    override fun getItemCount() = items.size

    fun submit(list: List<Booking>) {
        items = list
        notifyDataSetChanged()
    }


}