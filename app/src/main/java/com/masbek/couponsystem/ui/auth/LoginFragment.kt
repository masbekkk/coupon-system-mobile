package com.masbek.couponsystem.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.masbek.couponsystem.R
import com.masbek.couponsystem.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.progressLogin.visibility = View.VISIBLE
                }
                is LoginViewModel.LoginState.Success -> {
                    binding.progressLogin.visibility = View.GONE
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
                is LoginViewModel.LoginState.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressLogin.visibility = View.GONE
                    state.fieldErrors?.get("email")?.firstOrNull()?.let {
                        binding.tilEmail.error = it
                    }
                    state.fieldErrors?.get("password")?.firstOrNull()?.let {
                        binding.tilPassword.error = it
                    }
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                is LoginViewModel.LoginState.Idle -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
