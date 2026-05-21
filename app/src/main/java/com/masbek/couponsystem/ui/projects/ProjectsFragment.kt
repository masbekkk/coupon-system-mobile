package com.masbek.couponsystem.ui.projects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.masbek.couponsystem.R
import com.masbek.couponsystem.data.model.Project
import com.masbek.couponsystem.databinding.FragmentProjectsBinding
import com.masbek.couponsystem.databinding.ItemProjectBinding
import com.masbek.couponsystem.util.CurrencyFormatter
import com.masbek.couponsystem.util.StatusBadgeHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectsFragment : Fragment() {

    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProjectsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvProjects.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadProjects() }

        binding.fabCreate.setOnClickListener {
            findNavController().navigate(R.id.action_projects_to_createProject)
        }

        binding.rvProjects.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                val total = lm.itemCount
                val lastVisible = lm.findLastVisibleItemPosition()
                if (lastVisible >= total - 3) viewModel.loadMore()
            }
        })

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is ProjectsViewModel.ProjectsState.Loading -> {
                    binding.progressProjects.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is ProjectsViewModel.ProjectsState.Success -> {
                    binding.progressProjects.visibility = View.GONE
                    if (state.projects.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvProjects.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvProjects.visibility = View.VISIBLE
                        binding.rvProjects.adapter = ProjectAdapter(state.projects) { project ->
                            findNavController().navigate(
                                R.id.action_projects_to_projectDetail,
                                bundleOf("projectId" to project.id)
                            )
                        }
                    }
                }
                is ProjectsViewModel.ProjectsState.Error -> {
                    binding.progressProjects.visibility = View.GONE
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

class ProjectAdapter(
    private val projects: List<Project>,
    private val onViewDetails: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.VH>() {

    inner class VH(val binding: ItemProjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val project = projects[position]
        holder.binding.apply {
            tvCode.text = project.code
            tvName.text = project.name
            tvDescription.text = project.description ?: ""
            tvDescription.visibility = if (project.description.isNullOrBlank()) View.GONE else View.VISIBLE
            tvCoupons.text = "${CurrencyFormatter.formatNumber(project.totalCoupons)} kupon"
            StatusBadgeHelper.applyStatus(tvStatus, cardStatus, project.status)
            btnViewDetails.setOnClickListener { onViewDetails(project) }
        }
    }

    override fun getItemCount() = projects.size
}
