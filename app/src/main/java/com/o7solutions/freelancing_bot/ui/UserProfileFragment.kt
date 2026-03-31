package com.o7solutions.freelancing_bot.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.adapters.ExperienceAdapter
import com.o7solutions.freelancing_bot.data_classes.Experience
import com.o7solutions.freelancing_bot.databinding.FragmentUserProfileBinding
import com.o7solutions.freelancing_bot.utils.Constants
import java.net.URL
import java.util.concurrent.Executors

class UserProfileFragment : Fragment(), ExperienceAdapter.ItemClick {

    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var experienceAdapter: ExperienceAdapter
    private var experienceList = arrayListOf<Experience>()
    private lateinit var binding: FragmentUserProfileBinding
    private var userId = ""
    private var resumeUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("userId").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance().getReference(Constants.userCol)
        auth = FirebaseAuth.getInstance()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 1. Message/Chat Click
        binding.message.setOnClickListener {
            val contactEmail = binding.tvEmail.text.toString()
            if (contactEmail.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString(Constants.chatPersonKey, contactEmail)
                }
                findNavController().navigate(R.id.chatFragment, bundle)
            }
        }

        // 2. View Resume / PAN Card Click (Optimized for PDF visibility)
        binding.btnViewResume.setOnClickListener {
            if (!resumeUrl.isNullOrEmpty()) {
                try {
                    // Using Google Docs Viewer to ensure PDF is rendered in browser
                    val viewerUrl = "https://docs.google.com/viewer?url=$resumeUrl"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewerUrl))
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to direct URL if viewer fails
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resumeUrl))
                    startActivity(intent)
                }
            } else {
                Toast.makeText(requireContext(), "No document available", Toast.LENGTH_SHORT).show()
            }
        }

        experienceAdapter = ExperienceAdapter(experienceList, this)
        binding.experienceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.experienceRecyclerView.adapter = experienceAdapter

        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }

        loadData()
    }

    private fun loadData() {
        if (userId.isEmpty()) return
        binding.swipeRefresh.isRefreshing = true

        db.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                binding.apply {
                    tvName.text = snapshot.child("name").value?.toString() ?: "N/A"
                    tvEmail.text = snapshot.child("email").value?.toString() ?: ""
                    tvHeadline.text = snapshot.child("headline").value?.toString() ?: "Professional"
                    tvAbout.text = snapshot.child("about").value?.toString() ?: "No bio provided."

                    // Get Document URL (Check both 'resumeUrl' or 'panCardUrl' if applicable)
                    resumeUrl = snapshot.child("resumeUrl").value?.toString()

                    // Load Profile Image without Glide
                    val imageUrl = snapshot.child("profileImageUrl").value?.toString()
                    if (!imageUrl.isNullOrEmpty()) {
                        loadImageFromUrl(imageUrl)
                    } else {
                        ivProfile.setImageResource(R.drawable.profile3d)
                    }

                    // Process Experience
                    experienceList.clear()
                    snapshot.child("experience").children.forEach { child ->
                        val exp = child.getValue(Experience::class.java)
                        exp?.let { experienceList.add(it) }
                    }
                    experienceAdapter.notifyDataSetChanged()
                }
            }
            binding.swipeRefresh.isRefreshing = false
        }.addOnFailureListener {
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImageFromUrl(url: String) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            try {
                val inputStream = URL(url).openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)

                handler.post {
                    if (isAdded) { // Safety check to ensure fragment is still attached
                        binding.ivProfile.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                Log.e("UserProfile", "Image load failed: ${e.message}")
            }
        }
    }

    override fun onItemClick(position: Int) {}
    override fun onDeleteClick(position: Int) {}
}