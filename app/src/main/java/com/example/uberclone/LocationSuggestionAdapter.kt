package com.example.uberclone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.location.Address

class LocationSuggestionAdapter(
    private var suggestions: List<Address>,
    private val onSuggestionClicked: (Address) -> Unit
) : RecyclerView.Adapter<LocationSuggestionAdapter.SuggestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val address = suggestions[position]
        holder.bind(address)
        holder.itemView.setOnClickListener {
            onSuggestionClicked(address)
        }
    }

    override fun getItemCount(): Int = suggestions.size

    fun updateSuggestions(newSuggestions: List<Address>) {
        suggestions = newSuggestions
        notifyDataSetChanged()
    }

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)
        fun bind(address: Address) {
            textView.text = address.getAddressLine(0) ?: "Unknown location"
        }
    }
}