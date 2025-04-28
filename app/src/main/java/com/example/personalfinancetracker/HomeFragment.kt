package com.example.personalfinancetracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.app.DatePickerDialog
import android.content.Context
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date


class HomeFragment : Fragment() {

    private lateinit var transactionList: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter

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

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        transactionList = loadTransactions()
        adapter = TransactionAdapter(transactionList) { position ->
            transactionList.removeAt(position)
            adapter.notifyItemRemoved(position)
            saveTransactions()
        }


        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupType)
        val rbIncome = view.findViewById<RadioButton>(R.id.rbIncome)
        val rbExpense = view.findViewById<RadioButton>(R.id.rbExpense)
        val btnDate = view.findViewById<Button>(R.id.btnPickDate)
        val btnAdd = view.findViewById<Button>(R.id.btnAddTransaction)
        val rvTransactions = view.findViewById<RecyclerView>(R.id.rvTransactions)

        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvTransactions.adapter = adapter

        // Set up spinner values
        val categories = arrayOf("Food", "Transport", "Bills", "Other")
        spinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)

        // Default date
        var selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate = "$year-${month + 1}-$day"
                    btnDate.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        btnAdd.setOnClickListener {
            val title = etTitle.text.toString()
            val amountText = etAmount.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = spinner.selectedItem.toString()

            if (title.isNotEmpty() && amount != null && category.isNotEmpty()) {
                val isIncome = rbIncome.isChecked
                val finalAmount = if (isIncome) amount else -amount

                val type = if (isIncome) "income" else "expense"
                val newTransaction = Transaction(title, finalAmount, category, selectedDate, type)

                transactionList.add(newTransaction)
                adapter.notifyItemInserted(transactionList.size - 1)
                saveTransactions()

                etTitle.text.clear()
                etAmount.text.clear()
                radioGroup.clearCheck()
                rbIncome.isChecked = true
                Toast.makeText(requireContext(), "Transaction added!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Enter valid title and amount", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }



    private fun saveTransactions() {
        val sharedPref = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("loggedInEmail", "") ?: "unknown_user"
        val key = "${userEmail}_transactions"

        val gson = Gson()
        val json = gson.toJson(transactionList)
        sharedPref.edit().putString(key, json).apply()
    }

    private fun loadTransactions(): MutableList<Transaction> {
        val sharedPref = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("loggedInEmail", "") ?: "unknown_user"
        val key = "${userEmail}_transactions"


        val gson = Gson()
        val json = sharedPref.getString(key, null)
        val type = object : TypeToken<MutableList<Transaction>>() {}.type
        return if (json != null) gson.fromJson(json, type) else mutableListOf()
    }



}