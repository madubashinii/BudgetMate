package com.example.personalfinancetracker

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Dashboard : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvBudget: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var pieChart: PieChart
    private lateinit var tvSpentPercentage: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.deep_blue)

        WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView)
            .isAppearanceLightStatusBars = false  // white icons

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        // Views
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvBudget = view.findViewById(R.id.tvBudget)
        tvBalance = view.findViewById(R.id.tvBalance)
        tvIncome = view.findViewById(R.id.tvIncome)
        tvExpense = view.findViewById(R.id.tvExpense)
        progressBar = view.findViewById(R.id.progressBudget)
        pieChart = view.findViewById(R.id.pieChart)
        tvSpentPercentage = view.findViewById(R.id.tvSpentPercentage)

        return view
    }


    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("loggedInEmail", "") ?: "unknown_user"

        val transactionKey = "${email}_transactions"
        val budgetKey = "${email}_monthly_budget"
        val savedName = prefs.getString("name", "User")
        tvGreeting.text = "Hey, $savedName!"

        // Load transactions
        val gson = Gson()
        val json = prefs.getString(transactionKey, null)
        val type = object : TypeToken<List<Transaction>>() {}.type
        val transactions =
            if (json != null) gson.fromJson<List<Transaction>>(json, type) else emptyList()

        var income = 0.0
        var expense = 0.0

        transactions.forEach { t ->
            if (t.amount > 0) {
                income += t.amount
            } else {
                expense += -t.amount
            }
        }


        val balance = income - expense
        val budget = prefs.getFloat(budgetKey, 0f)
        val usedPercent = if (budget > 0) (expense / budget) * 100 else 0.0

        // Update UI
        tvIncome.text = "Rs. %.2f".format(income)
        tvExpense.text = "Rs. %.2f".format(expense)
        tvBalance.text = "Rs. %.2f".format(balance)
        tvBudget.text = "Rs. %.2f".format(budget)
        progressBar.progress = usedPercent.toInt().coerceAtMost(100)
        tvSpentPercentage.text = "%.0f%% Spent".format(usedPercent.coerceAtMost(100.0))

        // Update Pie Chart
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(income.toFloat(), "Income"))
        entries.add(PieEntry(expense.toFloat(), "Expense"))

        val dataSet = PieDataSet(entries, "Income vs Expenses")
        dataSet.setDrawValues(false)
        dataSet.sliceSpace = 2f
        dataSet.valueTextSize = 12f
        dataSet.setDrawIcons(false)

        // Use more neutral colors
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),
            Color.parseColor("#FF9800")
        )
        dataSet.valueTextColor = Color.BLACK

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(true)

        // Customize the PieChart
        pieChart.description.isEnabled = false
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.parseColor("#E0E0E0"))
        pieChart.setTransparentCircleAlpha(110)
        pieChart.animateY(1000)

        pieChart.invalidate()
    }
}