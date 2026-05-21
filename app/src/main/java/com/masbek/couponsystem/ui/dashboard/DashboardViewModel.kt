package com.masbek.couponsystem.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masbek.couponsystem.data.model.DashboardStats
import com.masbek.couponsystem.data.repository.DashboardRepository
import com.masbek.couponsystem.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _state = MutableLiveData<DashboardState>()
    val state: LiveData<DashboardState> = _state

    init {
        loadStats()
    }

    fun loadStats() {
        _state.value = DashboardState.Loading
        viewModelScope.launch {
            when (val result = dashboardRepository.getStats()) {
                is Result.Success -> _state.value = DashboardState.Success(result.data)
                is Result.Error -> _state.value = DashboardState.Error(result.message)
            }
        }
    }

    sealed class DashboardState {
        data object Loading : DashboardState()
        data class Success(val stats: DashboardStats) : DashboardState()
        data class Error(val message: String) : DashboardState()
    }
}
