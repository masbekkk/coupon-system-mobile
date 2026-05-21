package com.masbek.couponsystem.ui.batches

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masbek.couponsystem.data.model.BatchReport
import com.masbek.couponsystem.data.repository.BatchRepository
import com.masbek.couponsystem.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchReportViewModel @Inject constructor(
    private val batchRepository: BatchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val batchId: Int = savedStateHandle["batchId"] ?: 0
    val batchNumber: Int = savedStateHandle["batchNumber"] ?: 0
    val projectName: String = savedStateHandle["projectName"] ?: ""

    private val _state = MutableLiveData<ReportState>()
    val state: LiveData<ReportState> = _state

    init {
        loadReport()
    }

    fun loadReport() {
        _state.value = ReportState.Loading
        viewModelScope.launch {
            when (val result = batchRepository.getBatchReport(batchId)) {
                is Result.Success -> _state.value = ReportState.Success(result.data)
                is Result.Error -> _state.value = ReportState.Error(result.message)
            }
        }
    }

    sealed class ReportState {
        data object Loading : ReportState()
        data class Success(val report: BatchReport) : ReportState()
        data class Error(val message: String) : ReportState()
    }
}
