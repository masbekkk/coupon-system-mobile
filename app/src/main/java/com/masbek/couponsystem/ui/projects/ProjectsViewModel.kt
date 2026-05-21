package com.masbek.couponsystem.ui.projects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masbek.couponsystem.data.model.Project
import com.masbek.couponsystem.data.repository.ProjectRepository
import com.masbek.couponsystem.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _state = MutableLiveData<ProjectsState>()
    val state: LiveData<ProjectsState> = _state

    private val allProjects = mutableListOf<Project>()
    private var currentPage = 1
    private var hasNextPage = false
    private var isLoadingMore = false

    init {
        loadProjects()
    }

    fun loadProjects() {
        currentPage = 1
        allProjects.clear()
        _state.value = ProjectsState.Loading
        fetchPage(1)
    }

    fun loadMore() {
        if (isLoadingMore || !hasNextPage) return
        isLoadingMore = true
        fetchPage(currentPage + 1)
    }

    private fun fetchPage(page: Int) {
        viewModelScope.launch {
            when (val result = projectRepository.getProjects(page)) {
                is Result.Success -> {
                    val response = result.data
                    allProjects.addAll(response.data)
                    currentPage = response.meta?.currentPage ?: page
                    hasNextPage = response.links?.next != null
                    isLoadingMore = false
                    _state.value = ProjectsState.Success(
                        projects = allProjects.toList(),
                        hasMore = hasNextPage
                    )
                }
                is Result.Error -> {
                    isLoadingMore = false
                    if (allProjects.isEmpty()) {
                        _state.value = ProjectsState.Error(result.message)
                    }
                }
            }
        }
    }

    sealed class ProjectsState {
        data object Loading : ProjectsState()
        data class Success(val projects: List<Project>, val hasMore: Boolean) : ProjectsState()
        data class Error(val message: String) : ProjectsState()
    }
}
