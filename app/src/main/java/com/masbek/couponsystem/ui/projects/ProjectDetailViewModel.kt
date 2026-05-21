package com.masbek.couponsystem.ui.projects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masbek.couponsystem.data.model.*
import com.masbek.couponsystem.data.repository.BatchRepository
import com.masbek.couponsystem.data.repository.CouponRepository
import com.masbek.couponsystem.data.repository.ProjectRepository
import com.masbek.couponsystem.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val batchRepository: BatchRepository,
    private val couponRepository: CouponRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val projectId: Int = savedStateHandle["projectId"] ?: 0

    private val _projectState = MutableLiveData<ProjectDetailState>()
    val projectState: LiveData<ProjectDetailState> = _projectState

    private val _batchesState = MutableLiveData<BatchesState>()
    val batchesState: LiveData<BatchesState> = _batchesState

    private val _couponsState = MutableLiveData<CouponsState>()
    val couponsState: LiveData<CouponsState> = _couponsState

    private val _generateState = MutableLiveData<GenerateState>()
    val generateState: LiveData<GenerateState> = _generateState

    private val _deleteState = MutableLiveData<DeleteState>()
    val deleteState: LiveData<DeleteState> = _deleteState

    var couponPage = 1
    var couponPerPage = 25
    var couponSearch: String? = null
    var couponBatchId: Int? = null
    var couponPrizeTier: String? = null
    var couponSort: String? = null

    private var searchJob: Job? = null

    init {
        loadProject()
        loadBatches()
    }

    fun loadProject() {
        _projectState.value = ProjectDetailState.Loading
        viewModelScope.launch {
            when (val result = projectRepository.getProjectDetail(projectId)) {
                is Result.Success -> _projectState.value = ProjectDetailState.Success(result.data)
                is Result.Error -> _projectState.value = ProjectDetailState.Error(result.message)
            }
        }
    }

    fun loadBatches() {
        _batchesState.value = BatchesState.Loading
        viewModelScope.launch {
            when (val result = batchRepository.getProjectBatches(projectId)) {
                is Result.Success -> _batchesState.value = BatchesState.Success(result.data)
                is Result.Error -> _batchesState.value = BatchesState.Error(result.message)
            }
        }
    }

    fun loadCoupons() {
        _couponsState.value = CouponsState.Loading
        viewModelScope.launch {
            when (val result = couponRepository.getCoupons(
                projectId, couponPage, couponPerPage,
                couponSearch, couponBatchId, couponPrizeTier, couponSort
            )) {
                is Result.Success -> _couponsState.value = CouponsState.Success(result.data)
                is Result.Error -> _couponsState.value = CouponsState.Error(result.message)
            }
        }
    }

    fun searchCoupons(query: String?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            couponSearch = query
            couponPage = 1
            loadCoupons()
        }
    }

    fun nextCouponPage() {
        couponPage++
        loadCoupons()
    }

    fun prevCouponPage() {
        if (couponPage > 1) {
            couponPage--
            loadCoupons()
        }
    }

    fun generateBatch(batchId: Int, location: String) {
        _generateState.value = GenerateState.Loading
        viewModelScope.launch {
            when (val result = batchRepository.generateBatch(batchId, location)) {
                is Result.Success -> {
                    _generateState.value = GenerateState.Success
                    loadBatches()
                    loadProject()
                }
                is Result.Error -> _generateState.value = GenerateState.Error(result.message)
            }
        }
    }

    fun deleteProject() {
        _deleteState.value = DeleteState.Loading
        viewModelScope.launch {
            when (val result = projectRepository.deleteProject(projectId)) {
                is Result.Success -> _deleteState.value = DeleteState.Success
                is Result.Error -> _deleteState.value = DeleteState.Error(result.message)
            }
        }
    }

    fun getExportUrl(): String = couponRepository.buildExportUrl(projectId)

    sealed class ProjectDetailState {
        data object Loading : ProjectDetailState()
        data class Success(val project: Project) : ProjectDetailState()
        data class Error(val message: String) : ProjectDetailState()
    }

    sealed class BatchesState {
        data object Loading : BatchesState()
        data class Success(val batches: List<Batch>) : BatchesState()
        data class Error(val message: String) : BatchesState()
    }

    sealed class CouponsState {
        data object Loading : CouponsState()
        data class Success(val data: CouponListResponse) : CouponsState()
        data class Error(val message: String) : CouponsState()
    }

    sealed class GenerateState {
        data object Idle : GenerateState()
        data object Loading : GenerateState()
        data object Success : GenerateState()
        data class Error(val message: String) : GenerateState()
    }

    sealed class DeleteState {
        data object Idle : DeleteState()
        data object Loading : DeleteState()
        data object Success : DeleteState()
        data class Error(val message: String) : DeleteState()
    }
}
