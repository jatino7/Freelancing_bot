package com.o7solutions.freelancing_bot.ui.profile

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.adapters.ExperienceAdapter
import com.o7solutions.freelancing_bot.auth.LoginActivity
import com.o7solutions.freelancing_bot.data_classes.Experience
import com.o7solutions.freelancing_bot.databinding.FragmentProfileBinding
import com.o7solutions.freelancing_bot.utils.Constants
import com.o7solutions.freelancing_bot.utils.Functions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(), ExperienceAdapter.ItemClick {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentProfileBinding
    private lateinit var db: FirebaseFirestore
    private var auth = FirebaseAuth.getInstance()
    var list = arrayListOf<Experience>()
    lateinit var adapter: ExperienceAdapter

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

        adapter = ExperienceAdapter(list,this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.apply {

            swipeRefresh.setOnRefreshListener {
                getUserData()
                swipeRefresh.isRefreshing = false
            }


            addExp.setOnClickListener {
                showExperienceDialog {exp->
                    val expMap = mapOf(
                        "id" to exp.id,
                        "title" to exp.title,
                        "description" to exp.description
                    )
                    db.collection(Constants.userCol)
                        .document(auth.currentUser?.email.toString())
                        .update("experience", FieldValue.arrayUnion(expMap))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Experience added", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e->
                            Functions.showAlert(e.localizedMessage.toString(),requireContext())

                        }

                }

            }
            logOutBtn.setOnClickListener {
                auth.signOut()
                requireContext().getSharedPreferences(Constants.userKey, MODE_PRIVATE).edit().clear().apply()

                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        getUserData()
    }



    fun getUserData() {

        binding.apply {
            db.collection(Constants.userCol).document(auth.currentUser!!.email.toString())
                .get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {

                        binding.pgBar.visibility = View.GONE

                        binding.nameTV.text = documentSnapshot.getString("name")
                        binding.descriptionTV.text = "${documentSnapshot.getString("email")}\n${documentSnapshot.getString("description")}"

                        val experienceList = documentSnapshot.get("experience") as? List<Map<String, Any>>
                        list.clear()

                        experienceList?.forEach { expMap ->
                            val experience = Experience(
                                id = (expMap["id"] as? Number)?.toLong(),
                                title = expMap["title"] as? String,
                                description = expMap["description"] as? String
                            )
                            list.add(experience)
                            adapter.notifyDataSetChanged()

                            if(!list.isEmpty()) {
                                replaceText.visibility = View.GONE
                            }
                        }


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
        }
    }

    fun showExperienceDialog(onSave: (Experience) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_experience_input, null)

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.setCancelable(false)

        dialog.setOnShowListener {
            val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
            val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

            btnSave.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val desc = etDescription.text.toString().trim()

                if (title.isEmpty()) {
                    etTitle.error = "Title required"
                    return@setOnClickListener
                }

                if (desc.isEmpty()) {
                    etDescription.error = "Description required"
                    return@setOnClickListener
                }

                val experience = Experience(title = title, description = desc)
                onSave(experience)
                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
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