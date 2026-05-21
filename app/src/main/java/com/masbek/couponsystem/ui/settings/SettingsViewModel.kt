package com.masbek.couponsystem.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masbek.couponsystem.data.api.ApiService
import com.masbek.couponsystem.data.model.ChangePasswordRequest
import com.masbek.couponsystem.data.model.UpdateProfileRequest
import com.masbek.couponsystem.data.model.ApiErrorResponse
import com.masbek.couponsystem.data.repository.AuthRepository
import com.masbek.couponsystem.util.Result
import com.masbek.couponsystem.util.SessionManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val api: ApiService
) : ViewModel() {

    private val _logoutState = MutableLiveData<LogoutState>()
    val logoutState: LiveData<LogoutState> = _logoutState

    private val _profileState = MutableLiveData<ActionState>()
    val profileState: LiveData<ActionState> = _profileState

    private val _passwordState = MutableLiveData<ActionState>()
    val passwordState: LiveData<ActionState> = _passwordState

    fun getUserName(): String = sessionManager.getUserName() ?: ""
    fun getUserEmail(): String = sessionManager.getUserEmail() ?: ""
    fun getTheme(): String = sessionManager.getTheme()

    fun saveTheme(theme: String) {
        sessionManager.saveTheme(theme)
    }

    fun logout() {
        _logoutState.value = LogoutState.Loading
        viewModelScope.launch {
            authRepository.logout()
            _logoutState.value = LogoutState.Success
        }
    }

    fun updateProfile(name: String, email: String) {
        _profileState.value = ActionState.Loading
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.updateProfile(UpdateProfileRequest(name, email))
                }
                if (response.isSuccessful) {
                    sessionManager.saveUser(
                        sessionManager.getUserId() ?: "",
                        name,
                        email
                    )
                    _profileState.value = ActionState.Success("Profil berhasil disimpan")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val apiError = try {
                        Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    } catch (e: Exception) { null }
                    _profileState.value = ActionState.Error(apiError?.message ?: "Fitur belum tersedia")
                }
            } catch (e: Exception) {
                _profileState.value = ActionState.Error("Fitur belum tersedia")
            }
        }
    }

    fun changePassword(current: String, newPass: String, confirm: String) {
        if (current.isBlank() || newPass.isBlank() || confirm.isBlank()) {
            _passwordState.value = ActionState.Error("Semua field harus diisi")
            return
        }
        if (newPass != confirm) {
            _passwordState.value = ActionState.Error("Konfirmasi password tidak cocok")
            return
        }
        _passwordState.value = ActionState.Loading
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.changePassword(ChangePasswordRequest(current, newPass, confirm))
                }
                if (response.isSuccessful) {
                    _passwordState.value = ActionState.Success("Password berhasil diubah")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val apiError = try {
                        Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    } catch (e: Exception) { null }
                    _passwordState.value = ActionState.Error(apiError?.message ?: "Gagal mengubah password")
                }
            } catch (e: Exception) {
                _passwordState.value = ActionState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    sealed class LogoutState {
        data object Idle : LogoutState()
        data object Loading : LogoutState()
        data object Success : LogoutState()
    }

    sealed class ActionState {
        data object Idle : ActionState()
        data object Loading : ActionState()
        data class Success(val message: String) : ActionState()
        data class Error(val message: String) : ActionState()
    }
}
