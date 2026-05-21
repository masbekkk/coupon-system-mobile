package com.masbek.couponsystem.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masbek.couponsystem.data.repository.AuthRepository
import com.masbek.couponsystem.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email dan password harus diisi")
            return
        }
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> _loginState.value = LoginState.Success
                is Result.Error -> _loginState.value = LoginState.Error(
                    result.message,
                    result.fieldErrors
                )
            }
        }
    }

    sealed class LoginState {
        data object Idle : LoginState()
        data object Loading : LoginState()
        data object Success : LoginState()
        data class Error(
            val message: String,
            val fieldErrors: Map<String, List<String>>? = null
        ) : LoginState()
    }
}
