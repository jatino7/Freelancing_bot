package com.o7solutions.freelancing_bot.ui.profile

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.adapters.ExperienceAdapter
import com.o7solutions.freelancing_bot.auth.LoginActivity
import com.o7solutions.freelancing_bot.data_classes.Experience
import com.o7solutions.freelancing_bot.data_classes.User
import com.o7solutions.freelancing_bot.databinding.FragmentProfileBinding
import com.o7solutions.freelancing_bot.utils.AppwriteManager
import com.o7solutions.freelancing_bot.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

class ProfileFragment : Fragment(), ExperienceAdapter.ItemClick {

    private lateinit var binding: FragmentProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference(Constants.userCol)
    private lateinit var appwriteManager: AppwriteManager

    private var experienceList = ArrayList<Experience>()
    private lateinit var adapter: ExperienceAdapter
    private var currentUser: User? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadFile(it, ".jpg", isProfileImage = true) }
    }

    private val resumePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadFile(it, ".pdf", isProfileImage = false) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appwriteManager = AppwriteManager.getInstance(requireContext())

        setupRecyclerView()

        binding.apply {
            swipeRefresh.setOnRefreshListener {
                getUserData()
                swipeRefresh.isRefreshing = false
            }

            editProfileBtn.setOnClickListener { showEditProfileDialog() }
            profileIV.setOnClickListener { imagePicker.launch("image/*") }
            uploadResumeBtn.setOnClickListener { resumePicker.launch("application/pdf") }

            addExp.setOnClickListener {
                showExperienceDialog { newExp -> saveExperienceToDb(newExp) }
            }

            logOutBtn.setOnClickListener {
                auth.signOut()
                requireContext().getSharedPreferences(Constants.userKey, MODE_PRIVATE).edit().clear().apply()
                startActivity(Intent(requireActivity(), LoginActivity::class.java))
                requireActivity().finish()
            }
        }

        getUserData()
    }

    private fun setupRecyclerView() {
        adapter = ExperienceAdapter(experienceList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun getUserData() {
        val uid = auth.currentUser?.uid ?: return
        binding.pgBar.visibility = View.VISIBLE

        dbRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                binding.pgBar.visibility = View.GONE
                currentUser = snapshot.getValue(User::class.java)

                currentUser?.let { user ->
                    binding.nameTV.text = user.name
                    binding.descriptionTV.text = user.headline.ifEmpty { "Add a headline" }
                    binding.locationTV.text = user.location.ifEmpty { "Location not set" }
                    binding.aboutTV.text = user.about.ifEmpty { "No bio added yet." }

                    // Manually load image without Glide
                    if (user.profileImageUrl.isNotEmpty()) {
                        loadImageFromUrl(user.profileImageUrl, binding.profileIV)
                    }

                    binding.resumeNameTV.text = if (user.resumeUrl.isNotEmpty()) "Resume_Uploaded.pdf" else "No resume uploaded"

                    experienceList.clear()
                    experienceList.addAll(user.experience)
                    adapter.notifyDataSetChanged()
                    binding.replaceText.visibility = if (experienceList.isEmpty()) View.VISIBLE else View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.pgBar.visibility = View.GONE
            }
        })
    }

    /**
     * Helper to load images from Appwrite/Web without using Glide
     */
    private fun loadImageFromUrl(imageUrl: String, imageView: ImageView) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            try {
                val `in` = URL(imageUrl).openStream()
                val bitmap = BitmapFactory.decodeStream(`in`)

                handler.post {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    imageView.setImageResource(R.drawable.profile3d)
                }
            }
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etHeadline = dialogView.findViewById<TextInputEditText>(R.id.etHeadline)
        val etLocation = dialogView.findViewById<TextInputEditText>(R.id.etLocation)
        val etAbout = dialogView.findViewById<TextInputEditText>(R.id.etAbout)

        currentUser?.let {
            etName.setText(it.name)
            etHeadline.setText(it.headline)
            etLocation.setText(it.location)
            etAbout.setText(it.about)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updates = hashMapOf<String, Any>(
                    "name" to etName.text.toString(),
                    "headline" to etHeadline.text.toString(),
                    "location" to etLocation.text.toString(),
                    "about" to etAbout.text.toString()
                )
                dbRef.child(auth.currentUser?.uid!!).updateChildren(updates)
                    .addOnSuccessListener { Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadFile(uri: Uri, extension: String, isProfileImage: Boolean) {
        binding.pgBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val downloadUrl = appwriteManager.uploadImageFromUri(uri)
                val updateKey = if (isProfileImage) "profileImageUrl" else "resumeUrl"

                dbRef.child(auth.currentUser?.uid!!).child(updateKey).setValue(downloadUrl)
                    .addOnSuccessListener {
                        binding.pgBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Upload Successful", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                binding.pgBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveExperienceToDb(experience: Experience) {
        val uid = auth.currentUser?.uid ?: return
        experienceList.add(experience)
        dbRef.child(uid).child("experience").setValue(experienceList)
    }

    private fun showExperienceDialog(onSave: (Experience) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_experience_input, null)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = builder.create()

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etCompany = dialogView.findViewById<EditText>(R.id.etCompany)
        val etStartDate = dialogView.findViewById<EditText>(R.id.etStartDate)
        val etEndDate = dialogView.findViewById<EditText>(R.id.etEndDate)
        val cbCurrent = dialogView.findViewById<CheckBox>(R.id.cbIsCurrent)
        val etDesc = dialogView.findViewById<EditText>(R.id.etDescription)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        cbCurrent.setOnCheckedChangeListener { _, isChecked ->
            etEndDate.isEnabled = !isChecked
            if (isChecked) etEndDate.setText("Present") else etEndDate.setText("")
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val company = etCompany.text.toString().trim()

            if (title.isNotEmpty() && company.isNotEmpty()) {
                val experience = Experience(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    companyName = company,
                    startDate = etStartDate.text.toString(),
                    endDate = etEndDate.text.toString(),
                    isCurrentRole = cbCurrent.isChecked,
                    description = etDesc.text.toString()
                )
                onSave(experience)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onItemClick(position: Int) {
        // Implement edit/view logic for experience if needed
    }

    override fun onDeleteClick(position: Int) {
    }
}