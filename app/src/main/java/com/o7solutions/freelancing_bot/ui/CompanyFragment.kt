package com.o7solutions.freelancing_bot.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.databinding.FragmentCompanyBinding
import com.o7solutions.freelancing_bot.databinding.ItemCompanyBinding
import java.net.URL
import java.util.concurrent.Executors

// 1. Data Class
data class Company(
    val id: String = "",
    val name: String = "",
    val industry: String = "",
    val location: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val ownerId: String = ""
)

// 2. Fragment Class
class CompanyFragment : Fragment() {

    private var _binding: FragmentCompanyBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("Companies")

    private val companyList = ArrayList<Company>()
    private lateinit var adapter: CompanyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompanyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchUserCompanies()

        binding.fabAddCompany.setOnClickListener {
            showAddCompanyDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = CompanyAdapter(companyList) { company ->
//            navigateToDetails(company.id)
            showDetailsDialog(company)
        }

        binding.rvCompanies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CompanyFragment.adapter
        }
    }

    private fun showDetailsDialog(company: Company) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_company_details, null)

        val ivLogo = view.findViewById<ImageView>(R.id.ivDetailLogo)
        val tvName = view.findViewById<TextView>(R.id.tvDetailName)
        val tvIndustry = view.findViewById<TextView>(R.id.tvDetailIndustry)
        val tvLocation = view.findViewById<TextView>(R.id.tvDetailLocation)
        val tvDesc = view.findViewById<TextView>(R.id.tvDetailDesc)

        // Set Data
        tvName.text = company.name
        tvIndustry.text = company.industry.ifEmpty { "Industry not specified" }
        tvLocation.text = company.location.ifEmpty { "Remote/Not specified" }
        tvDesc.text = company.description.ifEmpty { "No description provided." }

        // Load Image using your native logic
        if (company.logoUrl.isNotEmpty()) {
            loadImageFromUrl(company.logoUrl, ivLogo)
        } else {
            ivLogo.setImageResource(R.drawable.profile3d)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton("Close", null)
            .show()
    }

    // Reuse your existing loading logic here or move it to a helper class
    private fun loadImageFromUrl(imageUrl: String, imageView: ImageView) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            try {
                val inputStream = URL(imageUrl).openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                handler.post {
                    val roundedDrawable = RoundedBitmapDrawableFactory.create(imageView.resources, bitmap)
                    roundedDrawable.isCircular = true
                    imageView.setImageDrawable(roundedDrawable)
                }
            } catch (e: Exception) {
                handler.post { imageView.setImageResource(R.drawable.profile3d) }
            }
        }
    }

    private fun fetchUserCompanies() {
        val currentUserId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        dbRef.orderByChild("ownerId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    binding.progressBar.visibility = View.GONE

                    companyList.clear()
                    for (data in snapshot.children) {
                        val company = data.getValue(Company::class.java)
                        company?.let { companyList.add(it) }
                    }

                    adapter.notifyDataSetChanged()
                    binding.tvNoData.visibility = if (companyList.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showAddCompanyDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_company, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etCompName)
        val etIndustry = dialogView.findViewById<TextInputEditText>(R.id.etCompIndustry)
        val etLocation = dialogView.findViewById<TextInputEditText>(R.id.etCompLocation)
        val etDesc = dialogView.findViewById<TextInputEditText>(R.id.etCompDesc)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Register Company")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = etName.text.toString().trim()
                val industry = etIndustry.text.toString().trim()
                val location = etLocation.text.toString().trim()
                val desc = etDesc.text.toString().trim()

                if (name.isNotEmpty()) {
                    saveCompanyToFirebase(name, industry, location, desc)
                } else {
                    Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCompanyToFirebase(name: String, industry: String, location: String, desc: String) {
        val companyId = dbRef.push().key ?: return
        val company = Company(
            id = companyId,
            name = name,
            industry = industry,
            location = location,
            description = desc,
            ownerId = auth.currentUser?.uid ?: ""
        )

        dbRef.child(companyId).setValue(company).addOnSuccessListener {
            Toast.makeText(context, "Company added!", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun navigateToDetails(companyId: String) {
//        val detailFragment = CompanyDetailsFragment()
//        detailFragment.arguments = Bundle().apply { putString("companyId", companyId) }
//
//        parentFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, detailFragment)
//            .addToBackStack(null)
//            .commit()
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 3. Adapter Class
class CompanyAdapter(
    private val list: List<Company>,
    private val onItemClick: (Company) -> Unit
) : RecyclerView.Adapter<CompanyAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCompanyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCompanyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val company = list[position]
        holder.binding.apply {
            tvCompanyName.text = company.name
            tvIndustry.text = company.industry

            if (company.logoUrl.isNotEmpty()) {
                loadImageFromUrl(company.logoUrl, ivCompanyLogo)
            } else {
                ivCompanyLogo.setImageResource(R.drawable.profile3d)
            }

            root.setOnClickListener { onItemClick(company) }
        }
    }

    override fun getItemCount() = list.size

    private fun loadImageFromUrl(imageUrl: String, imageView: ImageView) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            try {
                val inputStream = URL(imageUrl).openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                handler.post {
                    val roundedDrawable = RoundedBitmapDrawableFactory.create(imageView.resources, bitmap)
                    roundedDrawable.isCircular = true
                    imageView.setImageDrawable(roundedDrawable)
                }
            } catch (e: Exception) {
                handler.post { imageView.setImageResource(R.drawable.profile3d) }
            }
        }
    }
}