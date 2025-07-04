package com.o7solutions.freelancing_bot.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.databinding.FragmentViewJobBinding
import com.o7solutions.freelancing_bot.utils.Constants
import com.o7solutions.freelancing_bot.utils.Functions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewJobFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewJobFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentViewJobBinding
    private lateinit var db : FirebaseFirestore
    private var auth = FirebaseAuth.getInstance()
    var userId = ""
    var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_view_job, container, false)

        binding = FragmentViewJobBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        binding.toolbarView.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        title = arguments?.getString("title") ?: ""
        val description = arguments?.getString("description") ?: ""
        val cost = arguments?.getString("cost") ?: ""
        val deadline = arguments?.getString("deadline") ?: ""
        userId = arguments?.getString("userId") ?: ""
        val timestamp = arguments?.getLong("timestamp") ?: 0L

        // Bind the data to views
        binding.textJobTitle.text = title
        binding.textJobDescription.text = description
        binding.textJobCost.text = cost
        binding.textJobDeadline.text = deadline
        binding.textJobUserId.text = userId
        binding.textJobTimestamp.text = Functions.formatDate(timestamp)


        val userType = requireContext().getSharedPreferences(Constants.userKey, MODE_PRIVATE)
            .getInt("userType", -1)
        Log.d("Home Fragment", userType.toString())

        if (userType == 1) {
            binding.sendProposal.visibility = View.VISIBLE
        } else if (userType == 0) {
            binding.sendProposal.visibility = View.GONE
        }
        binding.sendProposal.setOnClickListener {

            showAlertForProposal(requireContext())
        }



    }



    private fun showAlertForProposal(context: Context) {
        val builder = AlertDialog.Builder(context)

        // Set the title of the dialog
        builder.setTitle("Enter Your Message")

        // Create a LinearLayout to hold the EditText (optional, but good for padding)
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        container.layoutParams = params

        val input = EditText(context)
        input.hint = "Type your message here..."
        input.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        container.addView(input)

        builder.setView(container)

        builder.setPositiveButton("Send") { dialog, which ->
            val message = input.text.toString()
            if (message.isNotBlank()) {

                val proposalData = Proposal(auth.currentUser!!.email.toString(),message, System.currentTimeMillis(), forJob = title )

                db.collection(Constants.proposalCol).document(userId)
                    .collection(userId).document().set(proposalData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Data added successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener { e->
                        Functions.showAlert("Unable to send proposal: $e",requireContext())
                    }

            } else {
                Toast.makeText(context, "Message is empty!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss() // Dismiss the dialog after action
        }

        // Set the negative button (Cancel)
        builder.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(context, "Operation Cancelled", Toast.LENGTH_SHORT).show()
            dialog.cancel() // Dismiss the dialog
        }

        val dialog = builder.create()
        dialog.show()
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewJobFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewJobFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
