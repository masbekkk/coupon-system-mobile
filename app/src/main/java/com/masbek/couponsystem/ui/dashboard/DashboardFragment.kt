package com.masbek.couponsystem.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.masbek.couponsystem.R
import com.masbek.couponsystem.data.model.ProjectSummary
import com.masbek.couponsystem.databinding.FragmentDashboardBinding
import com.masbek.couponsystem.util.CurrencyFormatter
import com.masbek.couponsystem.util.StatusBadgeHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadStats() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is DashboardViewModel.DashboardState.Loading -> {
                    binding.progressDashboard.visibility = View.VISIBLE
                }
                is DashboardViewModel.DashboardState.Success -> {
                    binding.progressDashboard.visibility = View.GONE
                    val stats = state.stats
                    binding.tvStatProjects.text = CurrencyFormatter.formatNumber(stats.totalProjects)
                    binding.tvStatBatches.text = CurrencyFormatter.formatNumber(stats.generatedBatches)
                    binding.tvStatCoupons.text = CurrencyFormatter.formatNumber(stats.totalCoupons)

                    val projects = stats.recentProjects ?: emptyList()
                    if (projects.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvRecentProjects.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvRecentProjects.visibility = View.VISIBLE
                        binding.rvRecentProjects.layoutManager = LinearLayoutManager(requireContext())
                        binding.rvRecentProjects.adapter = RecentProjectAdapter(projects) { project ->
                            findNavController().navigate(
                                R.id.action_dashboard_to_projectDetail,
                                bundleOf("projectId" to project.id)
                            )
                        }
                    }
                }
                is DashboardViewModel.DashboardState.Error -> {
                    binding.progressDashboard.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class RecentProjectAdapter(
    private val projects: List<ProjectSummary>,
    private val onDetailsClick: (ProjectSummary) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<RecentProjectAdapter.VH>() {

    inner class VH(val binding: com.masbek.couponsystem.databinding.ItemRecentProjectBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = com.masbek.couponsystem.databinding.ItemRecentProjectBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val project = projects[position]
        holder.binding.apply {
            tvCode.text = project.code
            tvName.text = project.name
            tvCoupons.text = "${CurrencyFormatter.formatNumber(project.totalCoupons)} kupon"
            tvCreatedBy.text = project.createdBy ?: ""
            StatusBadgeHelper.applyStatus(tvStatus, cardStatus, project.status)
            btnDetails.setOnClickListener { onDetailsClick(project) }
        }
    }

    override fun getItemCount() = projects.size
}
