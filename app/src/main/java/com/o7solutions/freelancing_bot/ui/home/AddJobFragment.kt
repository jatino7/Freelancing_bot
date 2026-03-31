package com.o7solutions.freelancing_bot.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.databinding.FragmentAddJobBinding
import com.o7solutions.freelancing_bot.utils.Constants

class AddJobFragment : Fragment() {

    private lateinit var binding: FragmentAddJobBinding
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference(Constants.jobCol)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupJobTypeDropdown()

        binding.apply {
            toolbarAdd.setNavigationOnClickListener { findNavController().popBackStack() }

            submitBtn.setOnClickListener {
                saveJobToDatabase()
            }
        }
    }

    private fun setupJobTypeDropdown() {
        val types = listOf("Full-time", "Part-time", "Contract", "Internship", "Remote")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, types)
        binding.jobTypeAutoComplete.setAdapter(adapter)
    }

    private fun saveJobToDatabase() {
        val title = binding.titleEditText.text.toString().trim()
        val company = binding.companyEditText.text.toString().trim()
        val location = binding.locationEditText.text.toString().trim()
        val jobType = binding.jobTypeAutoComplete.text.toString()
        val salary = binding.salaryEditText.text.toString().trim()
        val desc = binding.descriptionEditText.text.toString().trim()

        if (title.isEmpty() || company.isEmpty() || location.isEmpty() || jobType.isEmpty() || desc.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.pgBar.visibility = View.VISIBLE

        // Generate unique ID for the job listing
        val jobId = dbRef.push().key ?: System.currentTimeMillis().toString()

        val jobData = job(
            jobId = jobId,
            posterId = auth.currentUser?.uid ?: "",
            title = title,
            companyName = company,
            location = location,
            description = desc,
            jobType = jobType,
            salaryRange = salary,
            timestamp = System.currentTimeMillis(),
            status = 0 // Active
        )

        dbRef.child(jobId).setValue(jobData)
            .addOnSuccessListener {
                binding.pgBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Job Posted Successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                binding.pgBar.visibility = View.GONE
                Log.e("RealtimeDB", "Error: ${e.message}")
                Toast.makeText(requireContext(), "Failed to post job", Toast.LENGTH_SHORT).show()
            }
    }
}