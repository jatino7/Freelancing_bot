package com.o7solutions.freelancing_bot.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.o7solutions.freelancing_bot.R // Make sure this import is present
import com.o7solutions.freelancing_bot.adapters.ProposalAdapter
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.databinding.FragmentDashboardBinding
import com.o7solutions.freelancing_bot.utils.Constants

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private lateinit var adapter: ProposalAdapter
    private val proposalList = mutableListOf<Proposal>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchUserProposals()
    }

    private fun setupRecyclerView() {
        // Initialize adapter with click listener
        adapter = ProposalAdapter(proposalList) { selectedProposal ->
            val bundle = Bundle().apply {

                putString("applicantId",selectedProposal.applicationId)
                putString("jobId",selectedProposal.jobId)
                putString("appId", selectedProposal.applicationId)
                // Use the current user's ID as the recruiter ID
                // since the recruiter is the one viewing the dashboard
                putString("recruiterId", auth.currentUser?.uid)
            }

            // FIX: Ensure 'viewJobFragment' matches the ID in your nav_graph.xml
            findNavController().navigate(R.id.viewProposalFragment, bundle)
        }

        binding.rvProposals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DashboardFragment.adapter
        }
    }

    private fun fetchUserProposals() {
        val currentUserId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        // Path: Proposals -> currentUserId (This is where the recruiter's proposals are stored)
        db.getReference(Constants.proposalCol)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    proposalList.clear()
                    for (postSnapshot in snapshot.children) {
                        val proposal = postSnapshot.getValue(Proposal::class.java)
                        if (proposal != null) {
                            if(proposal.posterId == FirebaseAuth.getInstance().currentUser?.uid) {
                                proposalList.add(proposal)
                            }
                        }
                    }

                    binding.progressBar.visibility = View.GONE

                    if (proposalList.isEmpty()) {
                        binding.textNoProposals.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    } else {
                        binding.textNoProposals.visibility = View.GONE
                        proposalList.sortByDescending { it.timestamp }
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                }
            })
    }
}