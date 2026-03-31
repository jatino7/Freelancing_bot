package com.o7solutions.freelancing_bot.ui.dashboard

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.data_classes.User
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.databinding.FragmentViewProposalBinding
import com.o7solutions.freelancing_bot.utils.Constants
import java.net.URL
import java.util.concurrent.Executors

class ViewProposalFragment : Fragment() {

    private lateinit var binding: FragmentViewProposalBinding
    private val db = FirebaseDatabase.getInstance()
    private var proposal: Proposal? = null

    private val PROPOSAL_NODE = "freelyProposal"
    private val JOB_NODE = "freelyJobs"
    private val USER_NODE = "freelyUser"
    var email = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewProposalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val appId = it.getString("appId") ?: ""
            if (appId.isNotEmpty()) fetchProposalDetails(appId)
        }

        binding.chat.setOnClickListener{
            val contactEmail = email

            if (contactEmail.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString(Constants.chatPersonKey, contactEmail)
                }
                findNavController().navigate(R.id.chatFragment, bundle)
            }
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private fun fetchProposalDetails(appId: String) {
        binding.pgBar.visibility = View.VISIBLE
        db.getReference(PROPOSAL_NODE).child(appId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    proposal = snapshot.getValue(Proposal::class.java)
                    proposal?.let { p ->
                        binding.tvCoverLetter.text = p.coverLetter
                        fetchJobDetails(p.jobId)
                        fetchApplicantProfile(p.applicantId)
                    }
                }
                override fun onCancelled(error: DatabaseError) { binding.pgBar.visibility = View.GONE }
            })
    }

    private fun fetchJobDetails(jobId: String) {
        db.getReference(JOB_NODE).child(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val jobData = snapshot.getValue(job::class.java)
                    jobData?.let {
                        binding.tvJobTitle.text = it.title
                        binding.tvJobCompany.text = it.companyName
                        binding.tvJobDesc.text = it.description
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchApplicantProfile(uid: String) {
        db.getReference(USER_NODE).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.pgBar.visibility = View.GONE
                    val user = snapshot.getValue(User::class.java)
                    user?.let { u ->

                        email = u.email
                        binding.tvApplicantName.text = u.name
                        binding.tvApplicantHeadline.text = u.headline.ifEmpty { "Skilled Professional" }
                        if (u.profileImageUrl.isNotEmpty()) loadImage(u.profileImageUrl, binding.imgApplicant)

                        val finalUrl = if (!proposal?.resumeUrl.isNullOrEmpty()) proposal?.resumeUrl else u.resumeUrl

                        binding.btnViewResume.setOnClickListener {
                            if (!finalUrl.isNullOrEmpty()) {
                                openUrl(finalUrl)
                            } else {
                                Toast.makeText(context, "No resume link provided", Toast.LENGTH_SHORT).show()
                            }
                        }
                        renderExperience(u)
                    }
                }
                override fun onCancelled(error: DatabaseError) { binding.pgBar.visibility = View.GONE }
            })
    }

    private fun renderExperience(user: User) {
        binding.experienceContainer.removeAllViews()
        if (user.experience.isEmpty()) {
            val emptyTv = TextView(context).apply {
                text = "No work history provided."
                setTextColor(Color.LTGRAY)
                setPadding(10, 10, 10, 10)
            }
            binding.experienceContainer.addView(emptyTv)
            return
        }

        user.experience.forEach { exp ->
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 8, 0, 24)
            }

            val titleTv = TextView(context).apply {
                text = "• ${exp.title}"
                textSize = 16f
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
            }

            val companyTv = TextView(context).apply {
                text = "${exp.companyName} | ${exp.startDate} - ${exp.endDate}"
                textSize = 13f
                setTextColor(Color.parseColor("#7B1FA2"))
                setPadding(24, 2, 0, 0)
            }

            val descTv = TextView(context).apply {
                text = exp.description
                textSize = 14f
                setTextColor(Color.parseColor("#616161"))
                setPadding(24, 8, 0, 0)
                setLineSpacing(4f, 1f)
            }

            layout.addView(titleTv)
            layout.addView(companyTv)
            if (exp.description.isNotEmpty()) layout.addView(descTv)
            binding.experienceContainer.addView(layout)
        }
    }

        private fun openUrl(url: String) {
            if (url.isEmpty()) return

            try {
                val cleanUrl = url.trim()

                // This wrapper forces the PDF to render in a mobile-friendly viewer
                // especially useful for Appwrite/Firebase links that don't end in .pdf
                val googleDocsUrl = "https://docs.google.com/viewer?url=$cleanUrl"

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleDocsUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback to raw URL if the viewer fails
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (inner: Exception) {
                    Toast.makeText(requireContext(), "No browser found to open link", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun loadImage(url: String, imageView: ImageView) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                Handler(Looper.getMainLooper()).post { imageView.setImageBitmap(bitmap) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { imageView.setImageResource(R.drawable.profile3d) }
            }
        }
    }
}