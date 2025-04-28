package com.example.personalfinancetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class BudgetFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var isNotificationSent = false

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
        val view = inflater.inflate(R.layout.fragment_budget, container, false)
        sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("loggedInEmail", "") ?: "unknown_user"
        val budgetKey = "${userEmail}_monthly_budget"


        val etBudget = view.findViewById<EditText>(R.id.etBudgetAmount)
        val btnSave = view.findViewById<Button>(R.id.btnSaveBudget)
        val tvCurrent = view.findViewById<TextView>(R.id.tvCurrentBudget)
        val tvUsage = view.findViewById<TextView>(R.id.tvBudgetUsage)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBudget)


        // Load and display current budget
//        val currentBudget = sharedPreferences.getFloat("monthly_budget", 0f)
        val currentBudget = sharedPreferences.getFloat(budgetKey, 0f)
        tvCurrent.text = "Current Budget: Rs. %.2f".format(currentBudget)

        // Calculate and display budget usage
        showBudgetUsage(tvUsage, progressBar, currentBudget)

        btnSave.setOnClickListener {
            val input = etBudget.text.toString().toFloatOrNull()
            if (input != null) {
//                sharedPreferences.edit().putFloat("monthly_budget", input).apply()
                sharedPreferences.edit().putFloat(budgetKey, input).apply()
                tvCurrent.text = "Current Budget: Rs. %.2f".format(input)
                Toast.makeText(requireContext(), "Budget saved", Toast.LENGTH_SHORT).show()
                // Recalculate budget usage after saving
                showBudgetUsage(tvUsage, progressBar, input)


            } else {
                Toast.makeText(requireContext(), "Enter valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showBudgetUsage(tvUsage: TextView, progressBar: ProgressBar, budget: Float) {
        // Load transaction list from SharedPreferences
        val gson = Gson()
        val userEmail = sharedPreferences.getString("loggedInEmail", "") ?: "unknown_user"
        val transactionKey = "${userEmail}_transactions"
        val json = sharedPreferences.getString(transactionKey, null)


        val type = object : TypeToken<MutableList<Transaction>>() {}.type
        val transactionList: List<Transaction> =
            if (json != null) gson.fromJson(json, type) else emptyList()

        // Calculate total spending
        val totalSpent = transactionList
            .filter { it.type == "expense" }
            .sumOf { -it.amount } // negate because it's stored as negative



        // Calculate percentage
        val percentUsed = if (budget > 0) (totalSpent / budget) * 100 else 0.0

        // Update the text
        tvUsage.text =
            "Budget used: %.1f%% (Rs. %.2f / Rs. %.2f)".format(percentUsed, totalSpent, budget)
        progressBar.progress = percentUsed.toInt().coerceAtMost(100)

        // Color based on percentage
        when {
            percentUsed >= 100 -> {
                tvUsage.setTextColor(Color.RED)
                progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.RED)
                sendBudgetNotification(percentUsed)
                isNotificationSent = true
            }

            percentUsed >= 80 && !isNotificationSent-> {
                tvUsage.setTextColor(Color.parseColor("#FFA500")) // Orange
                progressBar.progressTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FFA500"))
                sendBudgetNotification(percentUsed)
                isNotificationSent = true
            }

            else -> {
                tvUsage.setTextColor(Color.parseColor("#4CAF50")) // Green
                progressBar.progressTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                isNotificationSent = false
            }
        }
    }


    private fun sendBudgetNotification(percentUsed: Double) {
        val channelId = "budget_alerts"
        val notificationId = 1001
        val context = requireContext()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notification for budget usage alerts"
            manager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // test built-in icon
            .setContentTitle("⚠ Budget Warning")
            .setContentText("You've used %.1f%% of your budget.".format(percentUsed))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }



}