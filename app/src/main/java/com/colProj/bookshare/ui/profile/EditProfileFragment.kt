package com.colProj.bookshare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.colProj.bookshare.R
import com.colProj.bookshare.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by activityViewModels()
    private var hasInjectedData = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        
        _binding?.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnSave.setOnClickListener {
                val name = etDisplayName.text.toString()
                val bio = etBio.text.toString()
                viewModel.updateProfile(name, bio)
            }
        }

        viewModel.fetchUser()
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            android.util.Log.d("EditProfileFragment", "User observed: $user, hasInjectedData: $hasInjectedData")
            if (!hasInjectedData) {
                user?.let { u ->
                    val initialName = u.displayName ?: u.email
                    val initialBio = if (u.bio.isNullOrBlank()) {
                        getString(R.string.profile_bio)
                    } else {
                        u.bio
                    }

                    binding.etDisplayName.setText(initialName)
                    binding.etBio.setText(initialBio)
                    
                    hasInjectedData = true
                    android.util.Log.d("EditProfileFragment", "Data injected: name=$initialName, bio=$initialBio")
                }
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
