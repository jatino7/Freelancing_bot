package com.o7solutions.freelancing_bot.ui

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.o7solutions.freelancing_bot.adapters.HomeAdapter
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.databinding.FragmentHomeBinding
import com.o7solutions.freelancing_bot.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

class JobsFragment : Fragment(), HomeAdapter.ItemClick {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.jobCol)

    private var fullList: ArrayList<job> = ArrayList()
    private var filteredList: ArrayList<job> = ArrayList()

    private lateinit var adapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        getJobs()
        setupSearch()
    }

    private fun setupUI() {
        val sharedPref = requireContext().getSharedPreferences(Constants.userKey, MODE_PRIVATE)
        val userType = sharedPref.getInt("userType", -1)

        // Show FAB only for Employers (userType 0)
        binding.fabAdd.visibility = if (userType == 0) View.VISIBLE else View.GONE

        binding.apply {
            fabAdd.setOnClickListener {
                findNavController().navigate(R.id.addJobFragment)
            }

            adapter = HomeAdapter(filteredList, this@JobsFragment)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterJobs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterJobs(query: String) {
        val searchText = query.lowercase(Locale.getDefault())
        filteredList.clear()

        if (searchText.isEmpty()) {
            filteredList.addAll(fullList)
        } else {
            for (item in fullList) {
                if (item.title?.lowercase()?.contains(searchText) == true ||
                    item.companyName?.lowercase()?.contains(searchText) == true) {
                    filteredList.add(item)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun getJobs() {
        binding.pgBar.visibility = View.VISIBLE

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return // Safety check for fragment lifecycle

                fullList.clear()
                val sharedPref = requireContext().getSharedPreferences(Constants.userKey, MODE_PRIVATE)
                val userType = sharedPref.getInt("userType", -1)
                val currentUid = FirebaseAuth.getInstance().uid

                if (snapshot.exists()) {
                    for (jobSnapshot in snapshot.children) {
                        val item = jobSnapshot.getValue(job::class.java)
                        item?.let {
                            // If user is Employer (0), show only their jobs. If Seeker (1), show all.
//                            if (userType == 0 && it.posterId == currentUid) {
//                                fullList.add(it)
//                            } else if (userType == 1) {
                                fullList.add(it)
//                            }
                        }
                    }
                    fullList.sortByDescending { it.timestamp }
                }

                filterJobs(binding.etSearch.text.toString())
                binding.pgBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                binding.pgBar.visibility = View.GONE
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onClicked(position: Int) {
        val selectedJob = filteredList[position]

        val bundle = Bundle().apply {
            putString("jobId", selectedJob.jobId)
            putString("title", selectedJob.title)
            putString("company", selectedJob.companyName)
            putString("location", selectedJob.location)
            putString("jobType", selectedJob.jobType)
            putString("salary", selectedJob.salaryRange)
            putString("description", selectedJob.description)
            putString("posterId", selectedJob.posterId)
            putLong("timestamp", selectedJob.timestamp)
        }
        findNavController().navigate(R.id.viewJobFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding to prevent memory leaks
    }
}