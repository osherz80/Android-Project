package com.colProj.bookshare.ui.search

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colProj.bookshare.R

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.tvUserName)
        val bookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val author: TextView = itemView.findViewById(R.id.tvAuthor)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val ratingBar: android.widget.RatingBar = itemView.findViewById(R.id.rbRating)
        val bookCover: android.widget.ImageView = itemView.findViewById(R.id.ivBookCover)
}