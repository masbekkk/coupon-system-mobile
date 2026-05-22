package com.masbek.couponsystem.ui.projects

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.masbek.couponsystem.R
import com.masbek.couponsystem.data.model.Batch
import com.masbek.couponsystem.data.model.Coupon
import com.masbek.couponsystem.data.model.Project
import com.masbek.couponsystem.databinding.FragmentProjectDetailBinding
import com.masbek.couponsystem.databinding.ItemBatchBinding
import com.masbek.couponsystem.databinding.ItemCouponBinding
import com.masbek.couponsystem.databinding.TabBatchesBinding
import com.masbek.couponsystem.databinding.TabCouponsBinding
import com.masbek.couponsystem.databinding.TabOverviewBinding
import com.masbek.couponsystem.util.CurrencyFormatter
import com.masbek.couponsystem.util.SessionManager
import com.masbek.couponsystem.util.StatusBadgeHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProjectDetailFragment : Fragment() {

    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProjectDetailViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    private var project: Project? = null
    private var batches: List<Batch> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnDelete.setOnClickListener { showDeleteDialog() }

        val tabTitles = listOf(
            getString(R.string.tab_overview),
            getString(R.string.tab_batches),
            getString(R.string.tab_coupons)
        )

        binding.viewPager.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount() = 3
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> OverviewTabFragment()
                    1 -> BatchesTabFragment()
                    2 -> CouponsTabFragment()
                    else -> OverviewTabFragment()
                }
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        viewModel.projectState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProjectDetailViewModel.ProjectDetailState.Loading -> {
                    binding.progressDetail.visibility = View.VISIBLE
                }
                is ProjectDetailViewModel.ProjectDetailState.Success -> {
                    binding.progressDetail.visibility = View.GONE
                    project = state.project
                    binding.tvProjectName.text = state.project.name
                    binding.tvProjectCode.text = state.project.code
                    StatusBadgeHelper.applyStatus(binding.tvStatus, binding.cardStatus, state.project.status)
                }
                is ProjectDetailViewModel.ProjectDetailState.Error -> {
                    binding.progressDetail.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProjectDetailViewModel.DeleteState.Success -> {
                    Snackbar.make(binding.root, R.string.project_deleted_success, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_projectDetail_to_projects)
                }
                is ProjectDetailViewModel.DeleteState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_title)
            .setMessage(R.string.dialog_delete_message)
            .setNegativeButton(R.string.btn_cancel, null)
            .setPositiveButton(R.string.btn_delete) { _, _ -> viewModel.deleteProject() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class OverviewTabFragment : Fragment() {
    private var _binding: TabOverviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val parentVM = (parentFragment as? ProjectDetailFragment)?.let {
            it.viewModels<ProjectDetailViewModel>().value
        } ?: return

        parentVM.projectState.observe(viewLifecycleOwner) { state ->
            if (state is ProjectDetailViewModel.ProjectDetailState.Success) {
                val p = state.project
                binding.tvTotalCoupons.text = CurrencyFormatter.formatNumber(p.totalCoupons)
                binding.tvCouponsPerBox.text = CurrencyFormatter.formatNumber(p.couponsPerBox ?: 0)
                binding.tvTotalBoxes.text = CurrencyFormatter.formatNumber(p.totalBoxes ?: 0)
                binding.tvBoxesPerBatch.text = CurrencyFormatter.formatNumber(p.boxesPerBatch ?: 0)
                binding.tvBatchCount.text = (p.totalBatches ?: 0).toString()
                binding.tvCreatedBy.text = p.createdBy ?: "-"
                binding.tvCreatedAt.text = p.createdAt ?: "-"
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class BatchesTabFragment : Fragment() {
    private var _binding: TabBatchesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabBatchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val parentVM = (parentFragment as? ProjectDetailFragment)?.let {
            it.viewModels<ProjectDetailViewModel>().value
        } ?: return

        binding.rvBatches.layoutManager = LinearLayoutManager(requireContext())

        parentVM.batchesState.observe(viewLifecycleOwner) { state ->
            if (state is ProjectDetailViewModel.BatchesState.Success) {
                binding.rvBatches.adapter = BatchAdapter(
                    state.batches,
                    onGenerate = { batch -> showGenerateDialog(parentVM, batch) },
                    onReport = { batch ->
                        findNavController().navigate(
                            R.id.action_projectDetail_to_batchReport,
                            bundleOf(
                                "batchId" to batch.id,
                                "batchNumber" to batch.batchNumber,
                                "projectName" to (parentVM.projectState.value as? ProjectDetailViewModel.ProjectDetailState.Success)?.project?.name.orEmpty()
                            )
                        )
                    },
                    onCoupons = {
                        val detailFragment = parentFragment as? ProjectDetailFragment
                        detailFragment?.view?.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)?.currentItem = 2
                    }
                )
            }
        }

        parentVM.generateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProjectDetailViewModel.GenerateState.Success -> {
                    Snackbar.make(binding.root, R.string.batch_generated_success, Snackbar.LENGTH_SHORT).show()
                }
                is ProjectDetailViewModel.GenerateState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun showGenerateDialog(vm: ProjectDetailViewModel, batch: Batch) {
        val context = requireContext()
        val density = resources.displayMetrics.density
        val marginHorizontal = (24 * density).toInt()
        val marginVertical = (8 * density).toInt()

        val frameLayout = android.widget.FrameLayout(context)
        val textInputLayout = com.google.android.material.textfield.TextInputLayout(
            context,
            null,
            com.google.android.material.R.attr.textInputStyle
        ).apply {
            hint = getString(R.string.hint_location)
            boxBackgroundMode = com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE
        }

        val input = com.google.android.material.textfield.TextInputEditText(textInputLayout.context).apply {
            id = View.generateViewId()
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            textSize = 15f
            isFocusable = true
            isFocusableInTouchMode = true
        }

        textInputLayout.addView(input)

        val lp = android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = marginHorizontal
            marginEnd = marginHorizontal
            topMargin = marginVertical
            bottomMargin = marginVertical
        }
        frameLayout.addView(textInputLayout, lp)

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.dialog_generate_title, batch.batchNumber))
            .setView(frameLayout)
            .setNegativeButton(R.string.btn_cancel, null)
            .setPositiveButton(R.string.btn_generate) { _, _ ->
                vm.generateBatch(batch.id, input.text.toString())
            }
            .create()

        // Clear FLAG_NOT_FOCUSABLE to allow keyboard interaction in portrait mode
        dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        dialog.setOnShowListener {
            input.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }

        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class CouponsTabFragment : Fragment() {
    private var _binding: TabCouponsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabCouponsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val parentVM = (parentFragment as? ProjectDetailFragment)?.let {
            it.viewModels<ProjectDetailViewModel>().value
        } ?: return

        binding.rvCoupons.layoutManager = LinearLayoutManager(requireContext())

        parentVM.projectState.observe(viewLifecycleOwner) { state ->
            if (state is ProjectDetailViewModel.ProjectDetailState.Success) {
                val isDraft = state.project.status.lowercase() == "draft"
                binding.tvCouponsDisabled.visibility = if (isDraft) View.VISIBLE else View.GONE
                binding.layoutFilters.visibility = if (isDraft) View.GONE else View.VISIBLE
                binding.layoutPagination.visibility = if (isDraft) View.GONE else View.VISIBLE
                if (!isDraft) {
                    parentVM.loadCoupons()
                    setupFilters(parentVM, state.project)
                }
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                parentVM.searchCoupons(s?.toString()?.takeIf { it.isNotBlank() })
            }
        })

        val perPageOptions = listOf(25, 50, 100, 250, 500)
        binding.spinnerPerPage.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, perPageOptions.map { it.toString() })
        binding.spinnerPerPage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                parentVM.couponPerPage = perPageOptions[pos]
                parentVM.couponPage = 1
                parentVM.loadCoupons()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        var sortAsc = true
        binding.btnSort.setOnClickListener {
            sortAsc = !sortAsc
            binding.btnSort.text = if (sortAsc) getString(R.string.sort_asc) else getString(R.string.sort_desc)
            parentVM.couponSort = if (sortAsc) "asc" else "desc"
            parentVM.couponPage = 1
            parentVM.loadCoupons()
        }

        binding.btnPrev.setOnClickListener { parentVM.prevCouponPage() }
        binding.btnNext.setOnClickListener { parentVM.nextCouponPage() }

        binding.btnExport.setOnClickListener { exportCoupons(parentVM) }

        parentVM.couponsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProjectDetailViewModel.CouponsState.Loading -> {
                    binding.progressCoupons.visibility = View.VISIBLE
                }
                is ProjectDetailViewModel.CouponsState.Success -> {
                    binding.progressCoupons.visibility = View.GONE
                    binding.rvCoupons.adapter = CouponAdapter(state.data.data)
                    val meta = state.data.meta
                    if (meta != null) {
                        binding.tvPageIndicator.text = getString(R.string.page_indicator, meta.currentPage, meta.lastPage)
                        binding.btnPrev.isEnabled = meta.currentPage > 1
                        binding.btnNext.isEnabled = meta.currentPage < meta.lastPage
                    }
                }
                is ProjectDetailViewModel.CouponsState.Error -> {
                    binding.progressCoupons.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupFilters(vm: ProjectDetailViewModel, project: Project) {
        val batchItems = mutableListOf(getString(R.string.filter_all_batches))
        val batchIds = mutableListOf<Int?>(null)
        // Populate from batches
        vm.batchesState.observe(viewLifecycleOwner) { state ->
            if (state is ProjectDetailViewModel.BatchesState.Success) {
                state.batches.forEach {
                    batchItems.add("Batch #${it.batchNumber}")
                    batchIds.add(it.id)
                }
                binding.spinnerBatch.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, batchItems)
            }
        }

        binding.spinnerBatch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                vm.couponBatchId = batchIds.getOrNull(pos)
                vm.couponPage = 1
                vm.loadCoupons()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val tierItems = mutableListOf(getString(R.string.filter_all_tiers))
        val tierIds = mutableListOf<Int?>(null)
        project.prizeTiers?.forEach {
            tierItems.add(it.name)
            tierIds.add(it.id)
        }
        binding.spinnerTier.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tierItems)
        binding.spinnerTier.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                vm.couponTierId = tierIds.getOrNull(pos)
                vm.couponPage = 1
                vm.loadCoupons()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun exportCoupons(vm: ProjectDetailViewModel) {
        val url = vm.getExportUrl()
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            addRequestHeader("Authorization", "Bearer ${(parentFragment as? ProjectDetailFragment)?.sessionManager?.getToken()}")
            addRequestHeader("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            setTitle("Coupon Export")
            setDescription(getString(R.string.export_started))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "coupons_${vm.projectId}.xlsx")
        }
        val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Snackbar.make(binding.root, R.string.export_started, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ─── Adapters ───

class BatchAdapter(
    private val batches: List<Batch>,
    private val onGenerate: (Batch) -> Unit,
    private val onReport: (Batch) -> Unit,
    private val onCoupons: (Batch) -> Unit
) : RecyclerView.Adapter<BatchAdapter.VH>() {

    inner class VH(val binding: ItemBatchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBatchBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val batch = batches[position]
        holder.binding.apply {
            tvBatchLabel.text = root.context.getString(R.string.batch_label, batch.batchNumber)
            StatusBadgeHelper.applyStatus(tvBatchStatus, cardBatchStatus, batch.status)

            val isCompleted = batch.status.lowercase() in listOf("completed", "ready")
            val isPending = batch.status.lowercase() in listOf("pending", "draft")

            tvBatchDescription.text = if (isCompleted) {
                listOfNotNull(batch.operator, batch.location, batch.generatedAt).joinToString(" • ")
            } else {
                root.context.getString(R.string.batch_pending)
            }

            btnGenerate.visibility = if (isPending) View.VISIBLE else View.GONE
            btnReport.visibility = if (isCompleted) View.VISIBLE else View.GONE
            btnCoupons.visibility = if (isCompleted) View.VISIBLE else View.GONE

            btnGenerate.setOnClickListener { onGenerate(batch) }
            btnReport.setOnClickListener { onReport(batch) }
            btnCoupons.setOnClickListener { onCoupons(batch) }
        }
    }

    override fun getItemCount() = batches.size
}

class CouponAdapter(
    private val coupons: List<Coupon>
) : RecyclerView.Adapter<CouponAdapter.VH>() {

    inner class VH(val binding: ItemCouponBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCouponBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val coupon = coupons[position]
        holder.binding.apply {
            tvSerial.text = coupon.serialNumber
            tvBox.text = coupon.boxNumber.toString()
            tvPosition.text = coupon.position.toString()
            tvPrizeTier.text = coupon.prizeTier ?: "-"
            tvAmount.text = CurrencyFormatter.formatRupiah(coupon.amount)
            StatusBadgeHelper.applyPrizeBadge(tvPrizeTier, cardPrize, coupon.amount)
        }
    }

    override fun getItemCount() = coupons.size
}
