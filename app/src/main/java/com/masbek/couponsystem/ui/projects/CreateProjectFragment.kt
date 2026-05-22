package com.masbek.couponsystem.ui.projects

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.masbek.couponsystem.R
import com.masbek.couponsystem.databinding.FragmentCreateProjectBinding
import com.masbek.couponsystem.databinding.ItemTierBinding
import com.masbek.couponsystem.util.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateProjectFragment : Fragment() {

    private var _binding: FragmentCreateProjectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateProjectViewModel by viewModels()
    private lateinit var tierAdapter: TierAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateProjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        tierAdapter = TierAdapter(
            viewModel.tiers.value ?: mutableListOf(),
            { viewModel.totalBoxes },
            { index, name, amount, perBox -> viewModel.updateTier(index, name, amount, perBox) },
            { index -> viewModel.removeTier(index) },
            { updateTierValidation() }
        )
        binding.rvTiers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTiers.adapter = tierAdapter

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { syncFieldsToViewModel() }
        }

        binding.etName.addTextChangedListener(textWatcher)
        binding.etCode.addTextChangedListener(textWatcher)
        binding.etDescription.addTextChangedListener(textWatcher)
        binding.etTotalCoupons.addTextChangedListener(textWatcher)
        binding.etCouponsPerBox.addTextChangedListener(textWatcher)
        binding.etTotalBatches.addTextChangedListener(textWatcher)

        binding.btnAddTier.setOnClickListener {
            viewModel.addTier()
        }

        binding.btnSubmit.setOnClickListener {
            syncFieldsToViewModel()
            viewModel.submit()
        }

        var previousSize = viewModel.tiers.value?.size ?: 0
        viewModel.tiers.observe(viewLifecycleOwner) { tiers ->
            tierAdapter.updateData(tiers)
            updateTierValidation()
            if (tiers.size > previousSize) {
                binding.root.post {
                    binding.root.fullScroll(View.FOCUS_DOWN)
                }
            }
            previousSize = tiers.size
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CreateProjectViewModel.CreateState.Loading -> {
                    binding.btnSubmit.isEnabled = false
                    binding.progressCreate.visibility = View.VISIBLE
                }
                is CreateProjectViewModel.CreateState.Success -> {
                    binding.progressCreate.visibility = View.GONE
                    Snackbar.make(binding.root, R.string.project_created_success, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_createProject_to_projects)
                }
                is CreateProjectViewModel.CreateState.Error -> {
                    binding.btnSubmit.isEnabled = true
                    binding.progressCreate.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                is CreateProjectViewModel.CreateState.Idle -> {}
            }
        }

        updateTierValidation()
    }

    private fun syncFieldsToViewModel() {
        viewModel.projectName = binding.etName.text.toString()
        viewModel.projectCode = binding.etCode.text.toString()
        viewModel.description = binding.etDescription.text.toString()
        viewModel.totalCoupons = binding.etTotalCoupons.text.toString().toIntOrNull() ?: 0
        viewModel.couponsPerBox = binding.etCouponsPerBox.text.toString().toIntOrNull() ?: 0
        viewModel.totalBatches = binding.etTotalBatches.text.toString().toIntOrNull() ?: 0

        val newTotalBoxes = viewModel.totalBoxes
        val totalBoxesStr = newTotalBoxes.toString()
        if (binding.etTotalBoxes.text.toString() != totalBoxesStr) {
            binding.etTotalBoxes.setText(totalBoxesStr)
        }

        val boxesPerBatchStr = viewModel.boxesPerBatch.toString()
        if (binding.etBoxesPerBatch.text.toString() != boxesPerBatchStr) {
            binding.etBoxesPerBatch.setText(boxesPerBatchStr)
        }

        if (tierAdapter.totalBoxes != newTotalBoxes) {
            tierAdapter.totalBoxes = newTotalBoxes
            tierAdapter.notifyDataSetChanged()
        }
        updateTierValidation()
    }

    private fun updateTierValidation() {
        val sum = viewModel.getTierSum()
        val perBox = viewModel.couponsPerBox
        val isValid = sum == perBox && perBox > 0
        binding.tvTierValidation.text = if (isValid) {
            getString(R.string.tier_validation_valid, sum, perBox)
        } else {
            getString(R.string.tier_validation_invalid, sum, perBox)
        }
        binding.tvTierValidation.setTextColor(
            resources.getColor(if (isValid) R.color.valid_green else R.color.invalid_red, null)
        )
        binding.btnSubmit.isEnabled = viewModel.isValid()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TierAdapter(
    private var tiers: MutableList<CreateProjectViewModel.TierItem>,
    private val getTotalBoxes: () -> Int,
    private val onUpdate: (Int, String, Int, Int) -> Unit,
    private val onDelete: (Int) -> Unit,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<TierAdapter.VH>() {

    var totalBoxes: Int = getTotalBoxes()

    inner class VH(val binding: ItemTierBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTierBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tier = tiers[position]
        holder.binding.apply {
            etTierName.removeTextChangedListener(etTierName.tag as? TextWatcher)
            etTierAmount.removeTextChangedListener(etTierAmount.tag as? TextWatcher)
            etTierPerBox.removeTextChangedListener(etTierPerBox.tag as? TextWatcher)

            if (etTierName.text.toString() != tier.name) etTierName.setText(tier.name)
            if (etTierAmount.text.toString() != tier.amount.toString()) etTierAmount.setText(if (tier.amount == 0 && tier.name.isBlank()) "" else tier.amount.toString())
            if (etTierPerBox.text.toString() != tier.perBoxQty.toString()) etTierPerBox.setText(if (tier.perBoxQty == 0 && tier.name.isBlank()) "" else tier.perBoxQty.toString())
            tvTierTotal.text = CurrencyFormatter.formatNumber(tier.perBoxQty * totalBoxes)

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val pos = holder.adapterPosition
                    if (pos == RecyclerView.NO_POSITION) return
                    val n = etTierName.text.toString()
                    val a = etTierAmount.text.toString().toIntOrNull() ?: 0
                    val p = etTierPerBox.text.toString().toIntOrNull() ?: 0
                    onUpdate(pos, n, a, p)
                    tvTierTotal.text = CurrencyFormatter.formatNumber(p * totalBoxes)
                    onChanged()
                }
            }

            etTierName.addTextChangedListener(watcher)
            etTierAmount.addTextChangedListener(watcher)
            etTierPerBox.addTextChangedListener(watcher)

            etTierName.tag = watcher
            etTierAmount.tag = watcher
            etTierPerBox.tag = watcher

            btnDeleteTier.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDelete(pos)
                }
            }
        }
    }

    override fun getItemCount() = tiers.size

    fun updateData(newTiers: MutableList<CreateProjectViewModel.TierItem>) {
        tiers = newTiers
        totalBoxes = getTotalBoxes()
        notifyDataSetChanged()
    }
}
