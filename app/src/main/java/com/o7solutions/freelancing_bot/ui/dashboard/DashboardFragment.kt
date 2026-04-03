package com.o7solutions.freelancing_bot.ui.dashboard

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.adapters.ProposalAdapter
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.data_classes.job // Using your job class
import com.o7solutions.freelancing_bot.databinding.FragmentDashboardBinding
import com.o7solutions.freelancing_bot.utils.Constants
import org.dhatim.fastexcel.Workbook
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private lateinit var adapter: ProposalAdapter
    private val proposalList = mutableListOf<Proposal>()

    // SEPARATE MAP TO HOLD JOB OBJECTS (Key: JobID, Value: Job Object)
    private val jobDetailsMap = HashMap<String, job>()

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

        binding.btnExportExcel.setOnClickListener {
            prepareExcelExport()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProposalAdapter(proposalList) { selectedProposal ->
            val bundle = Bundle().apply {
                putString("applicantId", selectedProposal.applicantId)
                putString("jobId", selectedProposal.jobId)
                putString("appId", selectedProposal.applicationId)
                putString("recruiterId", auth.currentUser?.uid)
            }
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

        db.getReference(Constants.proposalCol)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val tempProposals = mutableListOf<Proposal>()

                    for (postSnapshot in snapshot.children) {
                        val proposal = postSnapshot.getValue(Proposal::class.java)
                        if (proposal != null && proposal.posterId == currentUserId) {
                            tempProposals.add(proposal)
                        }
                    }

                    if (tempProposals.isEmpty()) {
                        updateUI(emptyList())
                    } else {
                        // Fetch job details separately
                        fetchJobDetails(tempProposals)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun fetchJobDetails(proposals: List<Proposal>) {
        db.getReference(Constants.jobCol).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                jobDetailsMap.clear()

                proposals.forEach { proposal ->
                    val jobItem = snapshot.child(proposal.jobId).getValue(job::class.java)
                    if (jobItem != null) {
                        jobDetailsMap[proposal.jobId] = jobItem
                    }
                }

                proposalList.clear()
                proposalList.addAll(proposals)
                proposalList.sortByDescending { it.timestamp }
                updateUI(proposalList)
            }

            override fun onCancelled(error: DatabaseError) {
                updateUI(proposals)
            }
        })
    }

    private fun updateUI(list: List<Proposal>) {
        binding.progressBar.visibility = View.GONE
        if (list.isEmpty()) {
            binding.textNoProposals.visibility = View.VISIBLE
            binding.btnExportExcel.visibility = View.GONE
        } else {
            binding.textNoProposals.visibility = View.GONE
            binding.btnExportExcel.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
    }

    private fun prepareExcelExport() {
        if (proposalList.isEmpty()) {
            Toast.makeText(requireContext(), "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Recruitment_Report_$timeStamp.xlsx"

        try {
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = requireContext().contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { requireContext().contentResolver.openOutputStream(it) }
            } else {
                val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(filePath, fileName)
                outputStream = java.io.FileOutputStream(file)
            }

            outputStream?.use { stream ->
                generateExcel(stream)
                Toast.makeText(requireContext(), "Report downloaded successfully", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateExcel(outputStream: OutputStream) {
        val workbook = Workbook(outputStream, "FreelancingBot", "1.0")
        val worksheet = workbook.newWorksheet("Proposals")

        // 1. Headers (Matching your job data class fields)
        val headers = listOf(
            "Applicant Name", "Job Title", "Company", "Location",
            "Job Type", "Salary Range", "Status", "Date Applied", "Resume URL"
        )

        headers.forEachIndexed { index, title ->
            worksheet.value(0, index, title)
            worksheet.style(0, index).bold().set()
        }

        // 2. Data Rows
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        proposalList.forEachIndexed { index, proposal ->
            val row = index + 1

            // Look up the job object from our separate map
            val jobInfo = jobDetailsMap[proposal.jobId]

            worksheet.value(row, 0, proposal.applicantName)
            worksheet.value(row, 1, jobInfo?.title ?: "N/A")
            worksheet.value(row, 2, jobInfo?.companyName ?: "N/A")
            worksheet.value(row, 3, jobInfo?.location ?: "N/A")
            worksheet.value(row, 4, jobInfo?.jobType ?: "N/A")
            worksheet.value(row, 5, jobInfo?.salaryRange ?: "N/A")

            val statusText = when(proposal.status) {
                0 -> "Applied"
                1 -> "Reviewed"
                2 -> "Shortlisted"
                3 -> "Rejected"
                else -> "Unknown"
            }
            worksheet.value(row, 6, statusText)
            worksheet.value(row, 7, dateFormat.format(Date(proposal.timestamp)))
            worksheet.value(row, 8, proposal.resumeUrl)
        }

        workbook.finish()
    }
}