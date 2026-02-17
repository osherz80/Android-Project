package com.colProj.bookshare.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colProj.bookshare.R
import com.colProj.bookshare.data.model.Post

class PostsAdapter : RecyclerView.Adapter<PostViewHolder>() {
    private var posts: List<Post> = emptyList()

    fun setPosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    private var onItemClick: ((Post) -> Unit)? = null

    fun setOnItemClickListener(listener: (Post) -> Unit) {
        onItemClick = listener
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.userName.text = post.userName
        holder.bookTitle.text = post.bookTitle
        holder.author.text = if (post.author.isNotEmpty()) "by ${post.author}"
                             else "Unknown Author"
        holder.description.text = post.description
        holder.description.movementMethod = android.text.method.ScrollingMovementMethod()
        
        // Handle nested scroll inside RecyclerView
        holder.description.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and android.view.MotionEvent.ACTION_MASK) {
                android.view.MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
        }
        
        holder.ratingBar.rating = post.rating
        
        val secureUrl = post.imageUrl.replace("http:", "https:")
        if (secureUrl.isNotEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(secureUrl)
                .placeholder(R.color.input_bg)
                .into(holder.bookCover)
        } else {
            holder.bookCover.setImageResource(R.color.input_bg)
        }
        
        // Important: Click listener on itemView might conflict with touch listener on text if not careful, 
        // but since description captures touches, clicking outside it should still work.
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(post)
        }
    }

    override fun getItemCount() = posts.size


}
