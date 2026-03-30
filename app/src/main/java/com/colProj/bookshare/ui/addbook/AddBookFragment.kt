package com.colProj.bookshare.ui.addbook

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.colProj.bookshare.R
import com.colProj.bookshare.utils.Resource
import com.google.android.material.textfield.TextInputEditText

import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colProj.bookshare.utils.StatusResource
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class AddBookFragment : Fragment() {

    private val viewModel: AddBookViewModel by viewModels()
    private val args: AddBookFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<TextInputEditText>(R.id.etBookTitle)
        val etAuthor = view.findViewById<TextInputEditText>(R.id.etAuthor)
        val etBookSummary = view.findViewById<TextInputEditText>(R.id.etBookSummary)
        val etRecommendation = view.findViewById<TextInputEditText>(R.id.etRecommendation)
        val etImage = view.findViewById<TextInputEditText>(R.id.etImageUrl)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        val tilSearch = view.findViewById<TextInputLayout>(R.id.tilSearch)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearchQuery)
        val rvSearch = view.findViewById<RecyclerView>(R.id.rvSearchResults)
        val ivBookCover = view.findViewById<ImageView>(R.id.ivBookCover)


        etBookSummary.keyListener = null

        alignScroll(etBookSummary)
        alignScroll(etRecommendation)

        if (!args.postId.isNullOrEmpty()) {
            view.findViewById<TextView>(R.id.tvTitle).text = "Edit Recommendation"
            etTitle.setText(args.bookTitle)
            etTitle.isEnabled = false

            etAuthor.setText(args.author)
            etAuthor.isEnabled = false

            etBookSummary.setText(args.bookSummary)
            etBookSummary.isEnabled = false

            etImage.setText(args.imageUrl)
            etImage.isEnabled = false
            
            etRecommendation.setText(args.description) 
            
            ratingBar.rating = args.rating

            if (!args.imageUrl.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(this)
                    .load(args.imageUrl)
                    .placeholder(R.color.input_bg)
                    .into(ivBookCover)
            }

            view.findViewById<TextView>(R.id.etSearchQuery)?.visibility = View.GONE
            tilSearch.visibility = View.GONE
            rvSearch.visibility = View.GONE

            btnSave.text = "Update Post"
        } else if (!args.bookTitle.isNullOrEmpty()) {
            etTitle.setText(args.bookTitle)
            etTitle.isEnabled = false

            etAuthor.setText(args.author)
            etAuthor.isEnabled = false

            etBookSummary.setText(args.bookSummary)
            etBookSummary.isEnabled = false

            etImage.setText(args.imageUrl)
            etImage.isEnabled = false
            if (!args.imageUrl.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(this)
                    .load(args.imageUrl)
                    .placeholder(R.color.input_bg)
                    .into(ivBookCover)
            }

            view.findViewById<TextView>(R.id.etSearchQuery)?.visibility = View.GONE
            tilSearch.visibility = View.GONE
            rvSearch.visibility = View.GONE

            btnSave.text = "Submit Review"
        } 

        rvSearch.layoutManager = LinearLayoutManager(context)
        val adapter = SearchResultsAdapter { book ->
             etTitle.setText(book.volumeInfo.title)
             etBookSummary.setText(book.volumeInfo.description)
             etRecommendation.setText("") 
             val authors = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"
             etAuthor.setText(authors)

             val img = book.volumeInfo.imageLinks?.thumbnail ?: book.volumeInfo.imageLinks?.smallThumbnail
             val secureImg = img?.replace("http:", "https:")
             etImage.setText(secureImg)

             if (!secureImg.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(this@AddBookFragment)
                    .load(secureImg)
                    .placeholder(R.color.input_bg)
                    .into(ivBookCover)
             }

             rvSearch.visibility = View.GONE
        }
        rvSearch.adapter = adapter
        // Instant Search with Debounce
        var searchJob: kotlinx.coroutines.Job? = null
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    kotlinx.coroutines.delay(1000) 
                    s?.toString()?.let { query ->
                        if (query.length > 2) {
                            performSearch(query)
                        }
                    }
                }
            }
        })

        tilSearch.setEndIconOnClickListener {
            searchJob?.cancel()
            performSearch(etSearch.text.toString())
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchJob?.cancel()
                performSearch(etSearch.text.toString())
                true
            } else {
                false
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val author = etAuthor.text.toString()
            val bookSummary = etBookSummary.text.toString()
            val recommendation = etRecommendation.text.toString()
            val imageUrl = etImage.text.toString()
            val rating = ratingBar.rating

            if (title.isBlank()){
                Toast.makeText(context, "Please select a book from search", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (recommendation.isBlank()) {
                Toast.makeText(context, "Please add your recommendation", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addPost(title, bookSummary, recommendation, rating, imageUrl, author, args.postId)
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.addPostStatus.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                StatusResource.SUCCESS -> {
                    view.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(context, "Post Added!", Toast.LENGTH_SHORT).show()

                    findNavController().popBackStack()
                }
                StatusResource.ERROR -> {
                    view.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
                StatusResource.LOADING -> {
                    view.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                    btnSave.isEnabled = false
                }
            }
        }
        
        viewModel.searchResults.observe(viewLifecycleOwner) { resource ->
             when(resource) {
                 is Resource.Success -> {
                     adapter.submitList(resource.data ?: emptyList())
                     rvSearch.visibility = View.VISIBLE
                 }
                 is Resource.Error -> {
                     Toast.makeText(context, "Search failed: ${resource.message}", Toast.LENGTH_SHORT).show()
                 }
                 is Resource.Loading -> { }
             }
        }
    }

    fun alignScroll(editText: TextInputEditText) {
        editText.movementMethod = ScrollingMovementMethod()
        editText.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            viewModel.searchBooks(query)
        }
    }

}
