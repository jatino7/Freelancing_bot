package com.o7solutions.freelancing_bot.ui.home

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.adapters.HomeAdapter
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.databinding.FragmentHomeBinding
import com.o7solutions.freelancing_bot.utils.Constants
import com.o7solutions.freelancing_bot.utils.Functions

class HomeFragment : Fragment(), HomeAdapter.ItemClick {

    private lateinit var binding: FragmentHomeBinding
    var loading = false
    private var db = FirebaseFirestore.getInstance()
    private var dataList: ArrayList<job> = ArrayList()
    private lateinit var adapter: HomeAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        if (binding != null) {


//        fab visibility
            val userType = requireContext().getSharedPreferences(Constants.userKey, MODE_PRIVATE)
                .getInt("userType", -1)
            Log.d("Home Fragment", userType.toString())

            if (userType == 0) {
                binding.fabAdd.visibility = View.VISIBLE
            } else if (userType == 1) {
                binding.fabAdd.visibility = View.GONE
            }
//
//            binding.fabPerson.setOnClickListener {
//                Functions.showAlert("This is person fab button",requireContext())
//            }


            binding.apply {


//            add Job fragment
                fabAdd.setOnClickListener {
                    findNavController().navigate(R.id.addJobFragment)
                }


//            adapter

                adapter = HomeAdapter(dataList, this@HomeFragment)
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter

            }

            getJobs()

        }
    }


    fun showProgressBar() {
        loading = !loading


        if (binding != null) {


            if (loading) {
                binding.pgBar.visibility = View.VISIBLE
            } else {
                binding.pgBar.visibility = View.GONE

            }
        }
    }

    fun getJobs() {


        showProgressBar()
        dataList.clear()
        db.collection(Constants.jobCol)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                    showProgressBar()
                }

                if (value != error) {
                    for (doc in value) {
                        val newJob = doc.toObject(job::class.java)
                        dataList.add(newJob)

                    }

                    adapter.notifyDataSetChanged()
                    showProgressBar()
                }

            }
    }

    override fun onClicked(position: Int) {

        val bundle = Bundle().apply {

            val job = dataList[position]
            putString("title", job.title)
            putString("description", job.description)
            putString("cost", job.cost)
            putString("deadline", job.deadline)
            putString("userId", job.userId)
            putLong("timestamp", job.timestamp)
        }
        findNavController().navigate(R.id.viewJobFragment, bundle)
    }
}