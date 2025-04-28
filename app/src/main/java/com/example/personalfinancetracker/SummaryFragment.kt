package com.example.personalfinancetracker

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SummaryFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.deep_blue)

        WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView)
            .isAppearanceLightStatusBars = false

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_summary, container, false)

        val sharedPref = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("loggedInEmail", "") ?: "unknown_user"
        val key = "${userEmail}_transactions"
        val json = sharedPref.getString(key, null)

//        val json = sharedPref.getString("transactions", null)
        val gson = Gson()
        val type = object : TypeToken<MutableList<Transaction>>() {}.type
        val transactionList: List<Transaction> =
            if (json != null) gson.fromJson(json, type) else emptyList()

// Calculate totals
        var foodTotal = 0.0
        var transportTotal = 0.0
        var billsTotal = 0.0
        var otherTotal = 0.0

        for (t in transactionList) {
            if (t.type == "expense") {
                val amount = -t.amount // because it was stored as negative
                when (t.category) {
                    "Food" -> foodTotal += amount
                    "Transport" -> transportTotal += amount
                    "Bills" -> billsTotal += amount
                    "Other" -> otherTotal += amount
                }
            }
        }


// Set TextViews
        view.findViewById<TextView>(R.id.tvFood).text = "🍔 Food: Rs. %.2f".format(foodTotal)
        view.findViewById<TextView>(R.id.tvTransport).text =
            "🚌 Transport: Rs. %.2f".format(transportTotal)
        view.findViewById<TextView>(R.id.tvBills).text = "💡 Bills: Rs. %.2f".format(billsTotal)
        view.findViewById<TextView>(R.id.tvOther).text = "📦 Other: Rs. %.2f".format(otherTotal)


//        Bar chart Setting
        val barChart =
            view.findViewById<com.github.mikephil.charting.charts.BarChart>(R.id.barChart)

// Prepare data entries
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, foodTotal.toFloat()))
        entries.add(BarEntry(1f, transportTotal.toFloat()))
        entries.add(BarEntry(2f, billsTotal.toFloat()))
        entries.add(BarEntry(3f, otherTotal.toFloat()))

        val dataSet = BarDataSet(entries, "Category-wise Spending").apply {
            colors = listOf(
                Color.parseColor("#FF5722"),
                Color.parseColor("#03A9F4"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#9C27B0")
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f

        barChart.apply {
            data = barData
            setFitBars(true)
            description.isEnabled = false
            legend.isEnabled = false
            animateY(1000)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter =
                    IndexAxisValueFormatter(listOf("Food", "Transport", "Bills", "Other"))
                setDrawGridLines(false)
                textSize = 12f
            }

            axisLeft.apply {
                axisMinimum = 0f
                textSize = 12f
            }

            axisRight.isEnabled = false
            invalidate()
        }


        return view
    }

}