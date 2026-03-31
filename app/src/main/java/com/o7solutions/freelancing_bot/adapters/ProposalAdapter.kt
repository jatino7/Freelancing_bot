package com.o7solutions.freelancing_bot.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.databinding.ItemProposalBinding
import com.o7solutions.freelancing_bot.utils.Functions

class ProposalAdapter(
    private var list: List<Proposal>,
    private val onItemClick: (Proposal) -> Unit // Add this
) : RecyclerView.Adapter<ProposalAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProposalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProposalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val proposal = list[position]
        holder.binding.apply {
            textApplicantName.text = proposal.applicantName
            textCoverLetter.text = proposal.coverLetter
            textTimestamp.text = Functions.formatDate(proposal.timestamp)

            textStatus.text = when(proposal.status) {
                0 -> "Applied"
                1 -> "Reviewed"
                2 -> "Shortlisted"
                else -> "Rejected"
            }

            root.setOnClickListener { onItemClick(proposal) } // Set click listener
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Proposal>) {
        list = newList
        notifyDataSetChanged()
    }
}