package com.colProj.bookshare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.colProj.bookshare.databinding.FragmentHomeBinding

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.colProj.bookshare.ui.search.PostsAdapter
import com.colProj.bookshare.utils.Resource

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostsAdapter()
        binding.rvHomePosts.layoutManager = LinearLayoutManager(context)
        binding.rvHomePosts.adapter = adapter

        adapter.setOnItemClickListener { post ->
            val action = HomeFragmentDirections.actionHomeFragmentToBookDetailsFragment(
                bookTitle = post.bookTitle,
                author = post.author,
                imageUrl = post.localImagePath ?: post.imageUrl,
                bookSummary = post.bookSummary
            )
            findNavController().navigate(action)
        }

        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.setPosts(posts)
        }

        viewModel.syncStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    android.widget.Toast.makeText(requireContext(), resource.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
