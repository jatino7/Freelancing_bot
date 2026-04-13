package com.o7solutions.freelancing_bot.Admin.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.o7solutions.freelancing_bot.auth.LoginActivity
import com.o7solutions.freelancing_bot.data_classes.User
import com.o7solutions.freelancing_bot.databinding.FragmentAdminHomeBinding
import com.o7solutions.freelancing_bot.utils.Constants

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchDashboardStats()
        fetchCurrentAdminDetails()


        binding.logOut.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged Out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.editAdminBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Profile Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDashboardStats() {
        // 1. Fetch Total Jobs Count
        db.getReference(Constants.jobCol).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobCount = snapshot.childrenCount.toString()
                binding.totalJobsTV.text = jobCount
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // 2. Fetch Total Users Count
        db.getReference(Constants.userCol).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.childrenCount.toString()
                binding.totalUsersTV.text = userCount
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun fetchCurrentAdminDetails() {
        val uid = auth.currentUser?.uid ?: return

        db.getReference(Constants.userCol).child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        binding.apply {
                            adminNameTV.text = it.name
                            adminEmailTV.text = it.email

                            // Set Role Text based on the Int role
                            adminRoleChip.text = when(it.role) {
                                0 -> "Employer"
                                1 -> "Job Seeker"
                                2 -> "Administrator"
                                else -> "Member"
                            }

                            // Optional: If you use a library like Glide to load the profile image
                            /*
                            if (it.profileImageUrl.isNotEmpty()) {
                                Glide.with(requireContext())
                                    .load(it.profileImageUrl)
                                    .into(adminProfileIV)
                            }
                            */
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load admin profile", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}