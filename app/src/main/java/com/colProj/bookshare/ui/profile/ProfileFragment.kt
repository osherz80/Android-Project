package com.colProj.bookshare.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.colProj.bookshare.R
import com.colProj.bookshare.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val viewModel: ProfileViewModel by activityViewModels()

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            try {
                // Persist permission for local URIs
                requireContext().contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not supported
            }
            viewModel.updateProfilePicture(it.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        _binding?.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnEditImage.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            btnLogout.setOnClickListener {
                viewModel.logout {
                    findNavController().navigate(R.id.action_profileFragment_to_authFragment)
                }
            }

            btnEditProfile.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
            }

            itemPersonalDetails.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
            }

            statPosts.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_myPostsFragment)
            }
        }

        viewModel.fetchUser()
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            _binding?.let { b ->
                user?.let { u ->
                    b.tvUsername.text = u.displayName ?: u.email
                    b.tvBio.text = u.bio ?: getString(R.string.profile_bio)
                    
                    val imageToLoad = u.localImagePath ?: u.photoUrl
                    if (!imageToLoad.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageToLoad)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop()
                            .into(b.ivProfileImage)
                    } else {
                        b.ivProfileImage.setImageResource(R.drawable.ic_person)
                    }
                }
            }
        }

        viewModel.userPostsCount.observe(viewLifecycleOwner) { count ->
            _binding?.tvPostsCount?.text = count.toString()
        }

        viewModel.imageUpdateStatus.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (it) {
                    Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                }
                viewModel.clearImageStatus()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
