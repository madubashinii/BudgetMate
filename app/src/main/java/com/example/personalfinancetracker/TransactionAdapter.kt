package com.example.personalfinancetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactions: List<Transaction> ,
                         private val onDelete: (Int) -> Unit) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val amount: TextView = view.findViewById(R.id.tvAmount)
        val category: TextView = view.findViewById(R.id.tvCategory)
        val date: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.title.text = transaction.title
        holder.amount.text = "Rs. %.2f".format(transaction.amount)
        holder.category.text = transaction.category
        holder.date.text = transaction.date

        holder.itemView.findViewById<ImageView>(R.id.ivDelete).setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onDelete(position)
            }
        }
    }

    override fun getItemCount(): Int = transactions.size
}
