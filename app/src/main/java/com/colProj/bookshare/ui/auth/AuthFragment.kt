package com.colProj.bookshare.ui.auth

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.colProj.bookshare.R
import com.colProj.bookshare.databinding.FragmentAuthBinding
import com.colProj.bookshare.utils.Resource
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()
            viewModel.login(email, pass)
        }

        binding.ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye) 
            } else {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye)
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnToggleLogin.setOnClickListener {
            updateToggle(true)
        }

        binding.btnToggleSignup.setOnClickListener {
            updateToggle(false)
        }

        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(requireContext())
        
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.google_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext(),
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignIn(result: androidx.credentials.GetCredentialResponse) {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            viewModel.handleGoogleIdToken(idToken)
        }
    }

    private fun updateToggle(isLogin: Boolean) {
        if (isLogin) {
            binding.btnToggleLogin.setBackgroundResource(R.drawable.toggle_selected_bg)
            binding.btnToggleLogin.setTextColor(resources.getColor(R.color.black, null))
            binding.btnToggleSignup.setBackgroundResource(0)
            binding.btnToggleSignup.setTextColor(resources.getColor(R.color.text_secondary, null))
            binding.btnLogin.text = getString(R.string.log_in)
        } else {
            binding.btnToggleSignup.setBackgroundResource(R.drawable.toggle_selected_bg)
            binding.btnToggleSignup.setTextColor(resources.getColor(R.color.black, null))
            binding.btnToggleLogin.setBackgroundResource(0)
            binding.btnToggleLogin.setTextColor(resources.getColor(R.color.text_secondary, null))
            binding.btnLogin.text = getString(R.string.sign_up)
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnGoogle.isEnabled = false
                }
                is Resource.Success -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogle.isEnabled = true
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogle.isEnabled = true
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
