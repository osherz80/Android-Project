package com.colProj.bookshare.ui.addrecommendation

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.colProj.bookshare.R
import com.colProj.bookshare.databinding.FragmentAddRecommendationBinding
import com.colProj.bookshare.utils.Resource
import com.google.android.material.button.MaterialButton

class AddRecommendationFragment : Fragment() {

    private var _binding: FragmentAddRecommendationBinding? = null
    private val viewModel: AddRecommendationViewModel by viewModels()

    private var selectedRating = 0

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                // Persistent permissions for local URIs
                val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, flag)
            } catch (e: Exception) {
                Log.e("AddRecommendation", "Failed to take persistable permission", e)
            }
            viewModel.setSelectedImageUri(uri)
        } else {
            Log.d("AddRecommendation", "No media selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddRecommendationBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        _binding?.apply {
            btnClose.setOnClickListener {
                findNavController().navigateUp()
            }

            btnAddImage.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            cardBookCover.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            btnPublish.setOnClickListener {
                val title = etBookTitle.text.toString().trim()
                val author = etBookAuthor.text.toString().trim()
                val recommendation = etRecommendation.text.toString().trim()

                viewModel.submitRecommendation(title, author, selectedRating, recommendation)
            }

            setupRatingButtons()
        }
    }

    private fun setupRatingButtons() {
        _binding?.let { binding ->
            val ratingButtons = listOf(
                binding.btnRating1,
                binding.btnRating2,
                binding.btnRating3,
                binding.btnRating4,
                binding.btnRating5
            )

            ratingButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    updateRatingSelection(index + 1, ratingButtons)
                }
            }
        }
    }

    private fun updateRatingSelection(rating: Int, buttons: List<MaterialButton>) {
        selectedRating = rating
        buttons.forEachIndexed { index, button ->
            if (index < rating) {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.neon_green))
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_dark))
                button.strokeWidth = 0
            } else {
                button.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                button.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.text_secondary)
                button.strokeWidth = 2
            }
        }
    }

    private fun setupObservers() {
        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            _binding?.apply {
                if (uri != null) {
                    ivBookCover.isVisible = true
                    layoutUploadPlaceholder.isVisible = false
                    Glide.with(this@AddRecommendationFragment)
                        .load(uri)
                        .centerCrop()
                        .into(ivBookCover)
                    btnAddImage.text = getString(R.string.cd_edit_image)
                } else {
                    ivBookCover.isVisible = false
                    layoutUploadPlaceholder.isVisible = true
                    btnAddImage.text = getString(R.string.add_image)
                }
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { resource ->
            _binding?.apply {
                when (resource) {
                    is Resource.Loading -> {
                        btnPublish.isEnabled = false
                        btnPublish.text = "Publishing..."
                    }
                    is Resource.Success -> {
                        btnPublish.isEnabled = true
                        btnPublish.text = getString(R.string.publish_recommendation)
                        Toast.makeText(requireContext(), getString(R.string.recommendation_saved), Toast.LENGTH_SHORT).show()
                        viewModel.resetSaveStatus()
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        btnPublish.isEnabled = true
                        btnPublish.text = getString(R.string.publish_recommendation)
                        val errorMsg = if (resource.message == "VAL_ERROR") {
                            getString(R.string.fill_all_fields)
                        } else {
                            resource.message ?: getString(R.string.recommendation_save_error)
                        }
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                        viewModel.resetSaveStatus()
                    }
                    else -> {
                        // Idle state
                        btnPublish.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
