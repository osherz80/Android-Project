package com.colProj.bookshare.ui.details

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.colProj.bookshare.R
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.databinding.FragmentBookDetailsBinding
import com.colProj.bookshare.databinding.ItemRatingProgressBinding
import com.colProj.bookshare.ui.search.PostsAdapter

class BookDetailsFragment : Fragment() {

    private val viewModel: BookDetailsViewModel by viewModels()
    private val args: BookDetailsFragmentArgs by navArgs()
    private lateinit var adapter: PostsAdapter
    private var _binding: FragmentBookDetailsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookDetailsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.toolbar?.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        _binding?.tvDetailTitle?.text = args.bookTitle
        _binding?.tvDetailAuthor?.text = args.author
        _binding?.tvDescription?.text = args.bookSummary
        _binding?.tvDescription?.movementMethod = ScrollingMovementMethod()
        
        _binding?.tvDescription?.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and android.view.MotionEvent.ACTION_MASK) {
                android.view.MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
        }

        _binding.let {
            if (args.imageUrl.isNotEmpty()) {
                it?.let { it1 ->
                    Glide.with(this)
                        .load(args.imageUrl)
                        .placeholder(R.color.input_bg)
                        .circleCrop()
                        .into(it1.ivBookCoverLarge )
                }
            }
        }
        

        
        adapter = PostsAdapter()
        _binding?.rvReviews?.layoutManager = LinearLayoutManager(context)
        _binding?.rvReviews?.adapter = adapter
        _binding?.rvReviews?.isNestedScrollingEnabled = false

        viewModel.setBookTitle(args.bookTitle)
        
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.setPosts(posts)
            calculateStats(posts)
        }

        _binding?.btnAddReview?.setOnClickListener {
            val action = BookDetailsFragmentDirections.actionBookDetailsFragmentToAddBookFragment(
                bookTitle = args.bookTitle,
                author = args.author,
                imageUrl = args.imageUrl,
                bookSummary = args.bookSummary
            )
            findNavController().navigate(action)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun calculateStats(posts: List<Post>) {
        val tvAvg = _binding?.tvAvgRating
        val rbAvg = _binding?.rbAvgRating
        val tvCount = _binding?.tvReviewCount
        
        if (posts.isEmpty()) {
            tvAvg?.text = "0.0"
            rbAvg?.rating = 0f
            tvCount?.text = "0 reviews"
            
            resetProgressBar(_binding?.progress5, "5")
            resetProgressBar(_binding?.progress4, "4")
            resetProgressBar(_binding?.progress3, "3")
            resetProgressBar(_binding?.progress2, "2")
            resetProgressBar(_binding?.progress1, "1")
            resetProgressBar(_binding?.progress0, "0")
            return
        }
        
        val count = posts.size
        val sum = posts.map { it.rating }.sum()
        val avg = sum / count
        
        tvAvg?.text = String.format("%.1f", avg)
        rbAvg?.rating = avg
        tvCount?.text = "$count reviews"
        
        val counts = IntArray(6) 
        posts.forEach {
            val star = it.rating.toInt().coerceIn(0, 5)
            counts[star]++
        }

        val maxCount = counts.maxOrNull() ?: 1
        val max = if (maxCount > 0) maxCount else 1
        
        updateProgressBar(_binding?.progress5, "5", counts[5], max)
        updateProgressBar(_binding?.progress4, "4", counts[4], max)
        updateProgressBar(_binding?.progress3, "3", counts[3], max)
        updateProgressBar(_binding?.progress2, "2", counts[2], max)
        updateProgressBar(_binding?.progress1, "1", counts[1], max)
        updateProgressBar(_binding?.progress0, "0", counts[0], max)
    }
    
    private fun updateProgressBar(itemBinding: ItemRatingProgressBinding?, label: String, count: Int, max: Int) {
        itemBinding?.tvStarLabel?.text = label
        itemBinding?.progressBar?.max = max
        itemBinding?.progressBar?.progress = count
        itemBinding?.tvRatingCount?.text = count.toString()
    }
    
    private fun resetProgressBar(itemBinding: ItemRatingProgressBinding?, label: String) {
        itemBinding?.tvStarLabel?.text = label
        itemBinding?.progressBar?.progress = 0
        itemBinding?.tvRatingCount?.text = "0"
    }
}
