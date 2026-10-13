package com.example.healthhand.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.healthhand.R
import com.example.healthhand.activity.ClinicDetailsActivity
import com.example.healthhand.activity.MyBookingsActivity
import com.example.healthhand.activity.Profile
import com.example.healthhand.activity.ReportsActivity
import com.example.healthhand.activity.SearchTestsActivity

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun go(viewId: Int, target: Class<*>) {
            view.findViewById<View>(viewId).setOnClickListener {
                startActivity(Intent(requireContext(), target))
            }
        }

        go(R.id.ivProfile, Profile::class.java)
        go(R.id.cardSearch, SearchTestsActivity::class.java)
        go(R.id.tvSeeAllTests, SearchTestsActivity::class.java)
        go(R.id.testBlood, SearchTestsActivity::class.java)
        go(R.id.testUrine, SearchTestsActivity::class.java)
        go(R.id.testStool, SearchTestsActivity::class.java)
        go(R.id.testThyroid, SearchTestsActivity::class.java)
        go(R.id.tvSeeAllClinics, ClinicDetailsActivity::class.java)
        go(R.id.cardClinic, ClinicDetailsActivity::class.java)
        go(R.id.actionBookTest, SearchTestsActivity::class.java)
        go(R.id.actionMyBookings, MyBookingsActivity::class.java)
        go(R.id.actionReports, ReportsActivity::class.java)
    }
}