package com.masbek.couponsystem.ui.batches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.masbek.couponsystem.R
import com.masbek.couponsystem.data.model.BoxReport
import com.masbek.couponsystem.databinding.FragmentBatchReportBinding
import com.masbek.couponsystem.databinding.ItemBoxReportBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BatchReportFragment : Fragment() {

    private var _binding: FragmentBatchReportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BatchReportViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBatchReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.tvTitle.text = getString(R.string.batch_report_title, viewModel.batchNumber)
        binding.tvSubtitle.text = getString(R.string.batch_report_subtitle, viewModel.projectName)

        binding.rvBoxes.layoutManager = LinearLayoutManager(requireContext())

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BatchReportViewModel.ReportState.Loading -> {
                    binding.progressReport.visibility = View.VISIBLE
                    binding.layoutSummary.visibility = View.GONE
                }
                is BatchReportViewModel.ReportState.Success -> {
                    binding.progressReport.visibility = View.GONE
                    binding.layoutSummary.visibility = View.VISIBLE
                    val report = state.report
                    binding.tvOperator.text = report.operator ?: "-"
                    binding.tvLocation.text = report.location ?: "-"
                    binding.tvTotalBoxes.text = (report.totalBoxes ?: 0).toString()
                    binding.tvStatus.text = report.status.replaceFirstChar { it.uppercase() }
                    binding.rvBoxes.adapter = BoxReportAdapter(report.boxes ?: emptyList())
                }
                is BatchReportViewModel.ReportState.Error -> {
                    binding.progressReport.visibility = View.GONE
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

class BoxReportAdapter(
    private val boxes: List<BoxReport>
) : RecyclerView.Adapter<BoxReportAdapter.VH>() {

    inner class VH(val binding: ItemBoxReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBoxReportBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val box = boxes[position]
        holder.binding.apply {
            tvBoxLabel.text = root.context.getString(R.string.box_label, box.boxNumber)
            tvCouponCount.text = root.context.getString(R.string.box_coupon_count, box.couponCount)

            layoutDistribution.removeAllViews()
            box.prizeDistribution?.forEach { dist ->
                val row = LinearLayout(root.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 4, 0, 4)
                }
                val nameTV = TextView(root.context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    text = dist.tierName
                    textSize = 13f
                    setTextColor(context.getColor(R.color.on_surface_variant_light))
                }
                val countTV = TextView(root.context).apply {
                    text = dist.count.toString()
                    textSize = 13f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(context.getColor(R.color.navy_700))
                }
                row.addView(nameTV)
                row.addView(countTV)
                layoutDistribution.addView(row)
            }
        }
    }

    override fun getItemCount() = boxes.size
}
