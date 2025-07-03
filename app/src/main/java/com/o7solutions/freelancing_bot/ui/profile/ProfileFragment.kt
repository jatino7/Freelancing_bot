package com.o7solutions.freelancing_bot.ui.profile

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.auth.LoginActivity
import com.o7solutions.freelancing_bot.databinding.FragmentProfileBinding
import com.o7solutions.freelancing_bot.utils.Constants

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentProfileBinding
    private lateinit var db: FirebaseFirestore
    private var auth = FirebaseAuth.getInstance()

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
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pgBar.visibility = View.VISIBLE
        db = FirebaseFirestore.getInstance()

        binding.apply {
            db.collection(Constants.userCol).document(auth.currentUser!!.uid)
                .get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {

                        binding.pgBar.visibility = View.GONE

                        binding.nameTV.text = documentSnapshot.getString("name")
                        binding.descriptionTV.text = "${documentSnapshot.getString("email")}\n${documentSnapshot.getString("description")}"

                    } else {
                        binding.pgBar.visibility = View.GONE

                        binding.nameTV.text = "User data not found"
                        binding.descriptionTV.text = "User data not found"


                    }
                }.addOnFailureListener { exception ->

                    binding.pgBar.visibility = View.GONE

                    binding.nameTV.text =" "
                    binding.descriptionTV.text = "Error: ${exception.message}"
                }


            logOutBtn.setOnClickListener {
                auth.signOut()
                requireContext().getSharedPreferences(Constants.userKey, MODE_PRIVATE).edit().clear().apply()

                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
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
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}