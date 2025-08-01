package com.o7solutions.freelancing_bot.ui.notifications

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.adapters.MessageAdapter
import com.o7solutions.freelancing_bot.data_classes.Message
import com.o7solutions.freelancing_bot.databinding.FragmentChatBinding
import com.o7solutions.freelancing_bot.utils.Constants
import com.o7solutions.freelancing_bot.utils.Functions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val messages = ArrayList<Message>()
    private lateinit var adapter: MessageAdapter
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatPartnerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            chatPartnerId = it.getString(Constants.chatPersonKey).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.email ?: return

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        adapter = MessageAdapter(messages, currentUserId)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadMessages(currentUserId, chatPartnerId)

        binding.sendButton.setOnClickListener {
            val message = binding.editMessage.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Message field can't be empty", Toast.LENGTH_SHORT).show()
            } else {
                Functions.sendMessage(currentUserId,chatPartnerId,message,requireContext())
                binding.editMessage.text.clear()
                Log.e("Chat",currentUserId+chatPartnerId)
            }
        }
    }

    private fun loadMessages(currentUserId: String, chatPartnerId: String) {
        Log.d("ChatDebug", "loadMessages called with: $currentUserId -> $chatPartnerId")

        FirebaseFirestore.getInstance()
            .collection(Constants.msg)
            .document(currentUserId)
            .collection(chatPartnerId)
            .orderBy("time")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("ChatDebug", "Error fetching messages", error)
                    return@addSnapshotListener
                }

                messages.clear()
                snapshots?.toObjects(Message::class.java)?.let {
                    Log.d("ChatDebug", "Messages received: ${it.size}")
                    messages.addAll(it)
                    adapter.notifyDataSetChanged()
                } ?: run {
                    Log.w("ChatDebug", "No messages found or data is null")
                }
            }
    }

}