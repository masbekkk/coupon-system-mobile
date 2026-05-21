package com.masbek.couponsystem.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.masbek.couponsystem.R
import com.masbek.couponsystem.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etName.setText(viewModel.getUserName())
        binding.etEmail.setText(viewModel.getUserEmail())

        when (viewModel.getTheme()) {
            "light" -> binding.toggleTheme.check(R.id.btnLight)
            "dark" -> binding.toggleTheme.check(R.id.btnDark)
            else -> binding.toggleTheme.check(R.id.btnSystem)
        }

        binding.btnSaveProfile.setOnClickListener {
            viewModel.updateProfile(
                binding.etName.text.toString(),
                binding.etEmail.text.toString()
            )
        }

        binding.btnChangePassword.setOnClickListener {
            viewModel.changePassword(
                binding.etCurrentPassword.text.toString(),
                binding.etNewPassword.text.toString(),
                binding.etConfirmPassword.text.toString()
            )
        }

        binding.toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val theme = when (checkedId) {
                R.id.btnLight -> "light"
                R.id.btnDark -> "dark"
                else -> "system"
            }
            viewModel.saveTheme(theme)
            when (theme) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_logout) { _, _ -> viewModel.logout() }
                .show()
        }

        viewModel.logoutState.observe(viewLifecycleOwner) { state ->
            if (state is SettingsViewModel.LogoutState.Success) {
                findNavController().navigate(R.id.action_settings_to_login)
            }
        }

        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SettingsViewModel.ActionState.Success ->
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                is SettingsViewModel.ActionState.Error ->
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                else -> {}
            }
        }

        viewModel.passwordState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SettingsViewModel.ActionState.Success -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                    binding.etCurrentPassword.text?.clear()
                    binding.etNewPassword.text?.clear()
                    binding.etConfirmPassword.text?.clear()
                }
                is SettingsViewModel.ActionState.Error ->
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
