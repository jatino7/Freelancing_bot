package com.o7solutions.freelancing_bot.Admin.ui.gallery

import android.content.ContentValues
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.*
import com.o7solutions.freelancing_bot.data_classes.User
import com.o7solutions.freelancing_bot.databinding.FragmentGalleryBinding
import com.o7solutions.freelancing_bot.utils.Constants
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val userList = ArrayList<User>()
    private lateinit var dbRef: DatabaseReference

    private val fragmentPages = listOf(
        UserListFragment.newInstance(1), // Seeker
        UserListFragment.newInstance(0), // Employer
        UserListFragment.newInstance(null) // All
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbRef = FirebaseDatabase.getInstance().getReference(Constants.userCol)

        setupViewPager()
        fetchUsers()

        binding.downloadExcelFab.setOnClickListener {
            if (userList.isNotEmpty()) {
                exportToExcel()
            } else {
                Toast.makeText(requireContext(), "No users to export", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupViewPager() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragmentPages.size
            override fun createFragment(position: Int): Fragment = fragmentPages[position]
        }
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Seekers"
                1 -> "Employers"
                else -> "All Views"
            }
        }.attach()
    }

    private fun fetchUsers() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    user?.let { userList.add(it) }
                }
                fragmentPages.forEach { it.refreshData(userList) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun exportToExcel() {
        val fileName = "UserReport_${System.currentTimeMillis()}.xlsx"
        val mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = requireContext().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { resolver.openOutputStream(it) }
            } else {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadDir.exists()) downloadDir.mkdirs()
                val file = File(downloadDir, fileName)
                outputStream = FileOutputStream(file)
                MediaScannerConnection.scanFile(requireContext(), arrayOf(file.absolutePath), arrayOf(mimeType), null)
            }

            outputStream?.use { os ->
                val wb = Workbook(os, "AdminApp", "1.0")

                // Filter lists by role
                val seekers = userList.filter { it.role == 0 }
                val employers = userList.filter { it.role == 1 }
                val admins = userList.filter { it.role != 0 && it.role != 1 }

                // Create individual sheets
                writeUserSheet(wb, "Seekers", seekers)
                writeUserSheet(wb, "Employers", employers)
                writeUserSheet(wb, "Admins", admins)

                wb.finish()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Excel with separate sheets saved to Downloads", Toast.LENGTH_LONG).show()
                }
            } ?: throw Exception("Could not open output stream")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to write a sheet
    private fun writeUserSheet(wb: Workbook, sheetName: String, list: List<User>) {
        val ws = wb.newWorksheet(sheetName)

        // Write Headers
        ws.value(0, 0, "User ID")
        ws.value(0, 1, "Name")
        ws.value(0, 2, "Email")
        ws.value(0, 3, "Role Type")

        // Write Data
        list.forEachIndexed { index, user ->
            val row = index + 1
            ws.value(row, 0, user.id ?: "N/A")
            ws.value(row, 1, user.name ?: "N/A")
            ws.value(row, 2, user.email ?: "N/A")
            ws.value(row, 3, when(user.role) {
                0 -> "Seeker"
                1 -> "Employer"
                else -> "Admin"
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}