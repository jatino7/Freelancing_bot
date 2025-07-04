package com.o7solutions.freelancing_bot.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.adapters.ProposalAdapter
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.databinding.FragmentDashboardBinding
import com.o7solutions.freelancing_bot.utils.Constants
import com.o7solutions.freelancing_bot.utils.Functions
import com.o7solutions.freelancing_bot.utils.Functions.setupToolbarWithPop

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    var list = arrayListOf<Proposal>()
    lateinit var adapter: ProposalAdapter



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupToolbarWithPop(binding.root,"Proposals")

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        adapter = ProposalAdapter(list)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        getData()
    }





    fun getData() {
        binding.pgBar.visibility = View.VISIBLE
        db.collection(Constants.proposalCol)
            .document(auth.currentUser!!.email.toString())
            .collection(auth.currentUser!!.email.toString())
            .addSnapshotListener { value, error ->

                binding.pgBar.visibility = View.GONE

                if (error != null) {
                    return@addSnapshotListener
                }

                list.clear() // clear old data before adding new

                if (value != null) {
                    for (doc in value) {
                        val item = doc.toObject(Proposal::class.java)
                        list.add(0,item)
                    }

                    adapter.notifyDataSetChanged()

                    if (list.isEmpty()) {
                        binding.replaceText.visibility = View.VISIBLE
                    } else {
                        binding.replaceText.visibility = View.GONE
                    }
                }
            }
    }


}