package com.masbek.couponsystem.ui.projects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masbek.couponsystem.data.model.CreateProjectRequest
import com.masbek.couponsystem.data.model.TierRequest
import com.masbek.couponsystem.data.repository.ProjectRepository
import com.masbek.couponsystem.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _state = MutableLiveData<CreateState>()
    val state: LiveData<CreateState> = _state

    val tiers = MutableLiveData<MutableList<TierItem>>(mutableListOf())

    var projectName = "Promo Akhir Tahun"
    var projectCode = ""
    var description = ""
    var totalCoupons = 10000
    var couponsPerBox = 1000
    var totalBatches = 2

    val totalBoxes: Int get() = ceil(totalCoupons.toDouble() / couponsPerBox).toInt()
    val boxesPerBatch: Int get() = if (totalBatches > 0) ceil(totalBoxes.toDouble() / totalBatches).toInt() else 0

    init {
        initDefaultTiers()
    }

    private fun initDefaultTiers() {
        tiers.value = mutableListOf(
            TierItem("Rp100k", 100000, 5),
            TierItem("Rp50k", 50000, 10),
            TierItem("Rp20k", 20000, 25),
            TierItem("Rp10k", 10000, 50),
            TierItem("Rp5k", 5000, 100),
            TierItem("Belum Beruntung", 0, 810)
        )
    }

    fun addTier() {
        val current = tiers.value ?: mutableListOf()
        current.add(TierItem("", 0, 0))
        tiers.value = current
    }

    fun removeTier(index: Int) {
        val current = tiers.value ?: return
        if (index in current.indices) {
            current.removeAt(index)
            tiers.value = current
        }
    }

    fun updateTier(index: Int, name: String, amount: Int, perBoxQty: Int) {
        val current = tiers.value ?: return
        if (index in current.indices) {
            current[index] = TierItem(name, amount, perBoxQty)
            tiers.value = current
        }
    }

    fun getTierSum(): Int = tiers.value?.sumOf { it.perBoxQty } ?: 0

    fun isValid(): Boolean {
        if (projectName.isBlank() || projectCode.isBlank()) return false
        if (totalCoupons <= 0 || couponsPerBox <= 0 || totalBatches <= 0) return false
        if (getTierSum() != couponsPerBox) return false
        return tiers.value?.all { it.name.isNotBlank() } ?: false
    }

    fun submit() {
        if (!isValid()) return
        _state.value = CreateState.Loading
        viewModelScope.launch {
            val request = CreateProjectRequest(
                name = projectName,
                code = projectCode.uppercase(),
                description = description.ifBlank { null },
                totalCoupons = totalCoupons,
                couponsPerBox = couponsPerBox,
                totalBatches = totalBatches,
                prizeTiers = tiers.value?.map { TierRequest(it.name, it.amount, it.perBoxQty) }
                    ?: emptyList()
            )
            when (val result = projectRepository.createProject(request)) {
                is Result.Success -> _state.value = CreateState.Success
                is Result.Error -> _state.value = CreateState.Error(
                    result.message,
                    result.fieldErrors
                )
            }
        }
    }

    data class TierItem(
        var name: String,
        var amount: Int,
        var perBoxQty: Int
    )

    sealed class CreateState {
        data object Idle : CreateState()
        data object Loading : CreateState()
        data object Success : CreateState()
        data class Error(
            val message: String,
            val fieldErrors: Map<String, List<String>>? = null
        ) : CreateState()
    }
}
