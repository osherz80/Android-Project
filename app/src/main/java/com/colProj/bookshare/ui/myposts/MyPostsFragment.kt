package com.colProj.bookshare.ui.myposts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.colProj.bookshare.databinding.FragmentMyPostsBinding
import com.colProj.bookshare.ui.search.PostsAdapter
import com.colProj.bookshare.utils.Resource

class MyPostsFragment : Fragment() {

    private var _binding: FragmentMyPostsBinding? = null
    private val viewModel: MyPostsViewModel by viewModels()
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPostsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.toolbar?.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter = PostsAdapter(showDeleteButton = true)
        _binding?.rvMyPosts?.layoutManager = LinearLayoutManager(context)
        _binding?.rvMyPosts?.adapter = adapter

        adapter.onDeleteClick = { post ->
            viewModel.deletePost(post.id)
        }
        
        adapter.setOnItemClickListener { post ->
            // Navigate to details if needed
             val action = MyPostsFragmentDirections.actionMyPostsFragmentToBookDetailsFragment(
                 bookTitle = post.bookTitle,
                 author = post.author,
                 imageUrl = post.imageUrl,
                 bookSummary = post.description
             )
             findNavController().navigate(action)
        }

        viewModel.myPosts.observe(viewLifecycleOwner) { posts ->
            adapter.setPosts(posts)
            
            if (posts.isEmpty()) {
                _binding?.tvEmpty?.visibility = View.VISIBLE
            } else {
                _binding?.tvEmpty?.visibility = View.GONE
            }
        }
        
        viewModel.refreshStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                     _binding?.progressBar?.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                     _binding?.progressBar?.visibility = View.GONE
                }
                is Resource.Error -> {
                     _binding?.progressBar?.visibility = View.GONE
                     android.widget.Toast.makeText(context, resource.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
