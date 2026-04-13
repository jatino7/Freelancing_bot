package com.o7solutions.freelancing_bot.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.databinding.FragmentViewJobBinding
import com.o7solutions.freelancing_bot.utils.Constants
import com.o7solutions.freelancing_bot.utils.Functions

class ViewJobFragment : Fragment() {

    private lateinit var binding: FragmentViewJobBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    private var posterId = ""
    private var jobTitle = ""
    private var jobId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarView.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Retrieve LinkedIn-style parameters from Bundle
        arguments?.let { bundle ->
            jobId = bundle.getString("jobId") ?: ""
            jobTitle = bundle.getString("title") ?: ""
            posterId = bundle.getString("posterId") ?: ""

            binding.apply {
                textJobTitle.text = jobTitle
                textJobCompany.text = bundle.getString("company")
                textJobLocation.text = bundle.getString("location")
                textJobType.text = bundle.getString("jobType")
                textJobSalary.text = bundle.getString("salary")
                textJobDescription.text = bundle.getString("description")

                val timestamp = bundle.getLong("timestamp")
                textJobTimestamp.text = Functions.formatDate(timestamp)
            }
        }

        // Role-based visibility
        val sharedPref = requireContext().getSharedPreferences(Constants.userKey, MODE_PRIVATE)
        val userType = sharedPref.getInt("userType", -1)

        // 1 is Job Seeker (can apply), 0 is Employer (can delete)
        if (userType == 1) {
            binding.btnApply.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.GONE
        } else if (userType == 0) {
            binding.btnApply.visibility = View.GONE

            if(posterId == FirebaseAuth.getInstance().currentUser?.uid) {
                binding.btnDelete.visibility = View.VISIBLE

            }
        } else {
            binding.btnApply.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
        }

        binding.btnApply.setOnClickListener {
            showApplicationDialog(requireContext())
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Job")
            .setMessage("Are you sure you want to delete this job? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteJob()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteJob() {
        if (jobId.isEmpty()) {
            Toast.makeText(requireContext(), "Error: Job ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Path should match your Firebase Database structure for Jobs
        db.getReference("Jobs").child(jobId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Job deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Functions.showAlert("Failed to delete: ${e.localizedMessage}", requireContext())
            }
    }

    private fun showApplicationDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_apply_job, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etCoverLetter = dialogView.findViewById<EditText>(R.id.etCoverLetter)
        val etResumeUrl = dialogView.findViewById<EditText>(R.id.etResumeUrl)

        builder.setView(dialogView)
        builder.setTitle("Apply for $jobTitle")

        builder.setPositiveButton("Submit") { dialog, _ ->
            val name = etName.text.toString().trim()
            val coverLetter = etCoverLetter.text.toString().trim()
            val resume = etResumeUrl.text.toString().trim()

            if (name.isNotEmpty() && coverLetter.isNotEmpty()) {
                sendApplication(name, coverLetter, resume)
            } else {
                Toast.makeText(context, "Please fill in required fields", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun sendApplication(name: String, message: String, resume: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val applicationRef = db.getReference(Constants.proposalCol).push()
        val applicationId = applicationRef.key ?: System.currentTimeMillis().toString()

        val applicationData = Proposal(
            applicationId = applicationId,
            jobId = jobId,
            applicantId = currentUserId,
            applicantName = name,
            coverLetter = message,
            resumeUrl = resume,
            timestamp = System.currentTimeMillis(),
            status = 0,
            posterId = posterId
        )

        applicationRef.setValue(applicationData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Application sent to $jobTitle!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Functions.showAlert("Error: ${e.localizedMessage}", requireContext())
            }
    }
}