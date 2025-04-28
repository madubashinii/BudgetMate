package com.example.personalfinancetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class SettingsFragment : Fragment() {
    private lateinit var sharedPref: android.content.SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.deep_blue)

        WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView)
            .isAppearanceLightStatusBars = false

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val spinnerCurrency = view.findViewById<Spinner>(R.id.spinnerCurrency)
        val btnExport = view.findViewById<Button>(R.id.btnExport)
        val btnImport = view.findViewById<Button>(R.id.btnImport)

        val currencies = arrayOf("LKR", "USD", "EUR")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            currencies
        )
        spinnerCurrency.adapter = adapter

        sharedPref = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        // Load saved currency and set it on spinner
        val savedCurrency = sharedPref.getString("currency", "LKR")
        val selectedIndex = currencies.indexOf(savedCurrency)
        if (selectedIndex >= 0) {
            spinnerCurrency.setSelection(selectedIndex)
        }

        // Save currency when user selects new one
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCurrency = currencies[position]
                sharedPref.edit().putString("currency", selectedCurrency).apply()
                Toast.makeText(
                    requireContext(),
                    "Currency saved: $selectedCurrency",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnExport.setOnClickListener {
            val transactions =
                getTransactionsFromSharedPreferences(requireContext())  // Replace with actual method
            exportData(requireContext(), transactions)
        }

        btnImport.setOnClickListener {
            val transactions = importData(requireContext())
            if (transactions != null) {
                val sharedPref =
                    requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                val userEmail = sharedPref.getString("loggedInEmail", "") ?: "unknown_user"
                val transactionKey = "${userEmail}_transactions"

                val json = Gson().toJson(transactions)
                sharedPref.edit().putString(transactionKey, json).apply()

                Toast.makeText(requireContext(), "Data imported successfully!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "No data to import or import failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("isLoggedIn", false).apply()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    // Function to import data from JSON
    private fun importData(context: Context): List<Transaction>? {
        val file = File(context.filesDir, "backup_transactions.json")
        if (!file.exists()) {
            Toast.makeText(context, "No backup file found", Toast.LENGTH_SHORT).show()
            return null
        }

        return try {
            val json = file.readText()
            val transactionsType = object : TypeToken<MutableList<Transaction>>() {}.type
            val transactions: MutableList<Transaction> = Gson().fromJson(json, transactionsType)

            val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val userEmail = sharedPref.getString("loggedInEmail", "") ?: "unknown_user"
            val transactionKey = "${userEmail}_transactions"

            sharedPref.edit().putString(transactionKey, json).apply()

            Toast.makeText(context, "Import successful!", Toast.LENGTH_SHORT).show()
            transactions
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }


    // Function to export data as JSON
    private fun exportData(context: Context, transactions: List<Transaction>) {
        try {
            val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val userEmail = sharedPref.getString("loggedInEmail", "") ?: "unknown_user"
            val transactionKey = "${userEmail}_transactions"
            val transactionsJson = sharedPref.getString(transactionKey, "[]")

            val file = File(context.filesDir, "backup_transactions.json")
            file.writeText(transactionsJson!!)

            Toast.makeText(context, "Export success!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to save transactions to SharedPreferences
    private fun saveTransactionsToSharedPreferences(
        context: Context,
        transactions: List<Transaction>
    ) {
        val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = Gson()
        val json = gson.toJson(transactions)

        editor.putString("transactions", json)
        editor.apply()
    }

    // Function to get transactions from SharedPreferences
    private fun getTransactionsFromSharedPreferences(context: Context): List<Transaction> {
        val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("transactions", null)

        val gson = Gson()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}