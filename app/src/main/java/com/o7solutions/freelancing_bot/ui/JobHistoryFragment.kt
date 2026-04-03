package com.o7solutions.freelancing_bot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.o7solutions.freelancing_bot.adapters.AppliedJobAdapter
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.databinding.FragmentJobHistoryBinding
import com.o7solutions.freelancing_bot.utils.Constants

class JobHistoryFragment : Fragment() {

    private lateinit var binding: FragmentJobHistoryBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    // Data lists
    private val applicationList = mutableListOf<Proposal>()
    private val jobDetailsMap = HashMap<String, job>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJobHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchMyApplications()

        binding.toolbarHistory.setNavigationOnClickListener {
            // Optional: findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        binding.rvJobHistory.layoutManager = LinearLayoutManager(requireContext())
        // Adapter is set inside updateUI after data is ready
    }

    private fun fetchMyApplications() {
        val currentUserId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.textNoHistory.visibility = View.GONE

        // 1. Fetch all proposals from the 'freelyProposal' node
        db.getReference(Constants.proposalCol)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    val tempApps = mutableListOf<Proposal>()
                    for (postSnapshot in snapshot.children) {
                        val proposal = postSnapshot.getValue(Proposal::class.java)

                        // Filter: Only show jobs where the current user is the applicant
                        if (proposal != null && proposal.applicantId == currentUserId) {
                            tempApps.add(proposal)
                        }
                    }

                    if (tempApps.isEmpty()) {
                        updateUI(emptyList())
                    } else {
                        // 2. We have the applications, now get the Job details (Title, Company, etc.)
                        fetchJobDetailsForApps(tempApps)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun fetchJobDetailsForApps(proposals: List<Proposal>) {
        // Fetch from the 'Jobs' node (using Constants.jobCol)
        db.getReference(Constants.jobCol).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                jobDetailsMap.clear()

                proposals.forEach { proposal ->
                    // Map the job details using the jobId from the proposal
                    val jobItem = snapshot.child(proposal.jobId).getValue(job::class.java)
                    if (jobItem != null) {
                        jobDetailsMap[proposal.jobId] = jobItem
                    }
                }

                applicationList.clear()
                applicationList.addAll(proposals)
                applicationList.sortByDescending { it.timestamp }

                updateUI(applicationList)
            }

            override fun onCancelled(error: DatabaseError) {
                updateUI(proposals)
            }
        })
    }

    private fun updateUI(list: List<Proposal>) {
        binding.progressBar.visibility = View.GONE

        if (list.isEmpty()) {
            binding.textNoHistory.visibility = View.VISIBLE
            binding.rvJobHistory.visibility = View.GONE
        } else {
            binding.textNoHistory.visibility = View.GONE
            binding.rvJobHistory.visibility = View.VISIBLE

            // Initialize and set the new adapter
            val historyAdapter = AppliedJobAdapter(list, jobDetailsMap) { selectedApp ->
                // Handle item click (e.g., Navigate to Application Details)
                Toast.makeText(requireContext(), "Checking status for ${jobDetailsMap[selectedApp.jobId]?.title}", Toast.LENGTH_SHORT).show()
            }
            binding.rvJobHistory.adapter = historyAdapter
        }
    }
}