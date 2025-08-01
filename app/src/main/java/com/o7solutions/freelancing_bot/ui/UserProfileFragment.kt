package com.o7solutions.freelancing_bot.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.adapters.ExperienceAdapter
import com.o7solutions.freelancing_bot.data_classes.Experience
import com.o7solutions.freelancing_bot.databinding.FragmentUserProfileBinding
import com.o7solutions.freelancing_bot.utils.Constants

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserProfileFragment : Fragment(), ExperienceAdapter.ItemClick {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var experienceAdapter: ExperienceAdapter
    private var experienceList = arrayListOf<Experience>()
    private lateinit var binding: FragmentUserProfileBinding
    var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            email = it.getString("email").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.message.setOnClickListener {
            val bundle = Bundle().apply {
                putString(Constants.chatPersonKey,binding.tvEmail.text.toString())
            }

            findNavController().navigate(R.id.chatFragment,bundle)
        }

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val recyclerView = view.findViewById<RecyclerView>(R.id.experienceRecyclerView)

        experienceAdapter = ExperienceAdapter(experienceList,this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = experienceAdapter

        swipeRefresh.setOnRefreshListener {
            loadData()
        }

        swipeRefresh.isRefreshing = true
        loadData()

    }

    fun loadData() {

        binding.apply {


        db.collection(Constants.userCol).document(email)
            .get().addOnSuccessListener { doc ->

                Log.e("user profile", "under documnet")
                tvName.text = doc.getString("name")
                tvEmail.text = doc.getString("email")
                tvDescription.text = doc.getString("description")

                Log.e("user profile",tvEmail.text.toString())
                val expData = doc.get("experience") as? List<Map<String, Any>>
                experienceList.clear()
                expData?.forEach {
                    val exp = Experience(
                        id = (it["id"] as? Number)?.toLong(),
                        title = it["title"] as? String,
                        description = it["description"] as? String
                    )
                    experienceList.add(exp)
                }

                if (experienceList.isEmpty()) {
                    Toast.makeText(requireContext(), "No experience added by user yet!", Toast.LENGTH_SHORT).show()
                }
                experienceAdapter.notifyDataSetChanged()
                swipeRefresh.isRefreshing = false
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
                swipeRefresh.isRefreshing = false
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}