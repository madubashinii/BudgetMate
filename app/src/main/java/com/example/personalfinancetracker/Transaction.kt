package com.example.personalfinancetracker

data class Transaction(
    val title: String,
    val amount: Double,

    val category: String,
    val date: String,
    val type: String
)
