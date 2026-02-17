package com.colProj.bookshare.ui.addbook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colProj.bookshare.R
import com.colProj.bookshare.data.model.GoogleBookItem

class SearchResultsAdapter(private val onItemClick: (GoogleBookItem) -> Unit) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {
    private var books = listOf<GoogleBookItem>()
    
    fun submitList(list: List<GoogleBookItem>) {
        books = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.text1.text = book.volumeInfo.title ?: "Unknown Title"
        holder.text2.text = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"
        holder.itemView.setOnClickListener { onItemClick(book) }
    }

    override fun getItemCount() = books.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text1: TextView = view.findViewById(R.id.tvTitle)
        val text2: TextView = view.findViewById(R.id.tvAuthor)
    }
}
