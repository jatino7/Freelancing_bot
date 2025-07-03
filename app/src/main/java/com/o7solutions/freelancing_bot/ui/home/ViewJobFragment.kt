package com.o7solutions.freelancing_bot.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.databinding.FragmentViewJobBinding
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

        val title = arguments?.getString("title") ?: ""
        val description = arguments?.getString("description") ?: ""
        val cost = arguments?.getString("cost") ?: ""
        val deadline = arguments?.getString("deadline") ?: ""
        val userId = arguments?.getString("userId") ?: ""
        val timestamp = arguments?.getLong("timestamp") ?: 0L

        // Bind the data to views
        binding.textJobTitle.text = title
        binding.textJobDescription.text = description
        binding.textJobCost.text = cost
        binding.textJobDeadline.text = deadline
        binding.textJobUserId.text = userId
        binding.textJobTimestamp.text = Functions.formatDate(timestamp)
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