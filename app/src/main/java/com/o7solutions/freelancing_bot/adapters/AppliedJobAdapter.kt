package com.o7solutions.freelancing_bot.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.databinding.ItemAppliedJobBinding
import java.text.SimpleDateFormat
import java.util.*

class AppliedJobAdapter(
    private val list: List<Proposal>,
    private val jobMap: Map<String, job>,
    private val onClick: (Proposal) -> Unit
) : RecyclerView.Adapter<AppliedJobAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAppliedJobBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppliedJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val proposal = list[position]
        val jobInfo = jobMap[proposal.jobId]

        holder.binding.apply {
            // Set Job Details
            tvJobTitle.text = jobInfo?.title ?: "Unknown Position"
            tvCompanyName.text = jobInfo?.companyName ?: "Unknown Company"

            // Set Date
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvAppliedDate.text = "Applied on: ${sdf.format(Date(proposal.timestamp))}"

            // Handle Status UI
            val (statusText, colorRes) = when (proposal.status) {
                0 -> "Applied" to android.R.color.holo_blue_dark
                1 -> "Reviewed" to android.R.color.holo_orange_dark
                2 -> "Shortlisted" to android.R.color.holo_green_dark
                3 -> "Rejected" to android.R.color.holo_red_dark
                else -> "Pending" to android.R.color.darker_gray
            }

            tvStatusBadge.text = statusText
            tvStatusBadge.backgroundTintList = ContextCompat.getColorStateList(root.context, colorRes)

            root.setOnClickListener { onClick(proposal) }
        }
    }

    override fun getItemCount(): Int = list.size
}