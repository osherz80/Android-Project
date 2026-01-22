package com.colProj.bookshare.ui.auth

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.colProj.bookshare.R
import com.colProj.bookshare.databinding.FragmentAuthBinding
import com.colProj.bookshare.utils.Resource
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null

    private val viewModel: AuthViewModel by viewModels()
    private var isPasswordVisible = false
    private var isLoginMode = true 

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        _binding?.let { b ->
            b.btnLogin.setOnClickListener {
                val email = b.etEmail.text.toString()
                val pass = b.etPassword.text.toString()
                
                if (isLoginMode) {
                    viewModel.login(email, pass)
                } else {
                    viewModel.signup(email, pass)
                }
            }

            b.ivPasswordToggle.setOnClickListener {
                isPasswordVisible = !isPasswordVisible
                b.etPassword.transformationMethod = if (isPasswordVisible) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }
                b.ivPasswordToggle.setImageResource(if (isPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye) 
                b.etPassword.setSelection(b.etPassword.text.length)
            }

            b.btnToggleLogin.setOnClickListener { updateToggle(true) }
            b.btnToggleSignup.setOnClickListener { updateToggle(false) }
            b.btnGoogle.setOnClickListener { signInWithGoogle() }
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
        when (val credential = result.credential) {
            is GoogleIdTokenCredential -> {
                viewModel.handleGoogleIdToken(credential.idToken)
            }
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.handleGoogleIdToken(googleIdTokenCredential.idToken)
                    } catch (e: Exception) {
                        Log.e("AuthFragment", "Failed to parse Google ID Token", e)
                        Toast.makeText(context, "Failed to parse Google ID Token", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("AuthFragment", "Unrecognized custom credential type: ${credential.type}")
                    Toast.makeText(context, "Unrecognized custom credential type", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Log.d("AuthFragment", "Unrecognized credential type: ${credential.type}")
                Toast.makeText(context, "Unrecognized credential type", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateToggle(isLogin: Boolean) {
        isLoginMode = isLogin
        _binding?.apply {
            val loginBg = if (isLogin) R.drawable.toggle_selected_bg else 0
            val signupBg = if (isLogin) 0 else R.drawable.toggle_selected_bg
            
            btnToggleLogin.setBackgroundResource(loginBg)
            btnToggleSignup.setBackgroundResource(signupBg)
            
            val activeColor = resources.getColor(R.color.black, null)
            val inactiveColor = resources.getColor(R.color.text_secondary, null)
            
            btnToggleLogin.setTextColor(if (isLogin) activeColor else inactiveColor)
            btnToggleSignup.setTextColor(if (isLogin) inactiveColor else activeColor)
            
            btnLogin.text = getString(if (isLogin) R.string.log_in else R.string.sign_up)
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { resource ->
            _binding?.let { b ->
                when (resource) {
                    is Resource.Loading -> {
                        b.btnLogin.isEnabled = false
                        b.btnGoogle.isEnabled = false
                    }
                    is Resource.Success -> {
                        b.btnLogin.isEnabled = true
                        b.btnGoogle.isEnabled = true
                        Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_authFragment_to_homeFragment)
                    }
                    is Resource.Error -> {
                        b.btnLogin.isEnabled = true
                        b.btnGoogle.isEnabled = true
                        Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
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