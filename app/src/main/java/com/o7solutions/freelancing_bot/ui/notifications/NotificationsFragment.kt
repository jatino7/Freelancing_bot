package com.o7solutions.freelancing_bot.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.adapters.RecentMessageAdapter
import com.o7solutions.freelancing_bot.data_classes.RecentMessage
import com.o7solutions.freelancing_bot.databinding.FragmentNotificationsBinding
import com.o7solutions.freelancing_bot.utils.Constants
import com.o7solutions.freelancing_bot.utils.Functions
import kotlin.random.Random

class NotificationsFragment : Fragment(), RecentMessageAdapter.OnClick {

    private lateinit var binding: FragmentNotificationsBinding
    var list = arrayListOf<RecentMessage>()
    var db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()
    private lateinit var adapter: RecentMessageAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecentMessageAdapter(list,this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        getRecentMessages()
    }

    fun getRecentMessages() {

        binding.pgBar.visibility = View.VISIBLE
        db.collection(Constants.userCol).document(auth.currentUser?.email.toString())
            .collection(Constants.recMes).addSnapshotListener { snapshot,error ->

                if(error != null) {
                    Functions.showAlert(error.localizedMessage.toString(),requireContext())
                }

                list.clear()
                if(snapshot != null) {

                    for(doc in snapshot) {
                        var recMsg = doc.toObject(RecentMessage::class.java)
                        list.add(recMsg)
                    }
                    adapter.notifyDataSetChanged()

                    if(list.isEmpty()) {
                        Toast.makeText(requireContext(), "No recent messages yet!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        binding.pgBar.visibility = View.GONE

    }

    override fun visit(position: Int) {

        var email = ""
        if(list[position].senderId == auth.currentUser?.email.toString()) {
            email = list[position].receiverId.toString()
        } else {
            email = list[position].senderId.toString()

        }
        var bundle = Bundle()
        bundle.putString(Constants.chatPersonKey,email)
        findNavController().navigate(R.id.chatFragment,bundle)
    }
}