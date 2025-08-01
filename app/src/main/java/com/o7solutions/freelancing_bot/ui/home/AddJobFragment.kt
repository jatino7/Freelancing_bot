package com.o7solutions.freelancing_bot.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.databinding.FragmentAddJobBinding
import com.o7solutions.freelancing_bot.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddJobFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddJobFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentAddJobBinding
    private lateinit var db: FirebaseFirestore

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
        binding = FragmentAddJobBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        binding.apply {
            deadlineEditText.setOnClickListener {
                showDatePicker()
            }

            submitBtn.setOnClickListener {
                submitJobData()
            }

            toolbarAdd.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }


    private fun submitJobData() {
        with(binding) {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val cost = costEditText.text.toString().trim()
            val deadlineText = deadlineEditText.text.toString().trim()

            // Basic validation
            if (title.isEmpty() || description.isEmpty() || cost.isEmpty() || deadlineText.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return
            }

            val newJob = job(
                userId = FirebaseAuth.getInstance().currentUser!!.email.toString(),
                title = title,
                description = description,
                cost = cost,
                deadline = deadlineText.toString(),
                timestamp = System.currentTimeMillis()
            )

            pgBar.visibility = View.VISIBLE

            FirebaseFirestore.getInstance().collection(Constants.jobCol)
                .add(newJob)
                .addOnSuccessListener {
                    pgBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Job posted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    pgBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error posting job!", Toast.LENGTH_SHORT).show()
                    Log.e("Add",e.toString())
                }
        }
    }



    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")

//        builder.setTheme(R.style.ThemeOverlay_App_DatePicker)

        val datePicker = builder.build()

        datePicker.addOnPositiveButtonClickListener { selection ->

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = format.format(calendar.time)
            binding.deadlineEditText.setText(formattedDate)
        }

        datePicker.show(childFragmentManager, "DATE_PICKER")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddJobFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddJobFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}