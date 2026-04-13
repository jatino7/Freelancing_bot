package com.o7solutions.freelancing_bot.Admin.ui.slideshow

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.databinding.FragmentSlideshowBinding
import com.o7solutions.freelancing_bot.databinding.ItemJobAdminBinding
import com.o7solutions.freelancing_bot.utils.Constants
import org.dhatim.fastexcel.Workbook
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    private val jobList = ArrayList<job>()
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbRef = FirebaseDatabase.getInstance().getReference(Constants.jobCol)
        binding.jobsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fetchJobs()

        binding.downloadJobsExcelFab.setOnClickListener {
            if (jobList.isNotEmpty()) {
                exportJobsToExcel()
            } else {
                Toast.makeText(requireContext(), "No jobs to export", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchJobs() {
        binding.jobsProgressBar.visibility = View.VISIBLE
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                jobList.clear()
                for (data in snapshot.children) {
                    val jobItem = data.getValue(job::class.java)
                    jobItem?.let { jobList.add(it) }
                }
                binding.jobsRecyclerView.adapter = JobAdapter(jobList)
                binding.jobsProgressBar.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
                binding.jobsProgressBar.visibility = View.GONE
            }
        })
    }

    private fun exportJobsToExcel() {
        val fileName = "JobReport_${System.currentTimeMillis()}.xlsx"
        val mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Scoped Storage logic for Android 10+
                val resolver = requireContext().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { resolver.openOutputStream(it) }
            } else {
                // Legacy logic for Android 9 and below
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadDir.exists()) downloadDir.mkdirs()
                val file = File(downloadDir, fileName)
                outputStream = FileOutputStream(file)

                // Trigger Media Scanner
                MediaScannerConnection.scanFile(requireContext(), arrayOf(file.absolutePath), arrayOf(mimeType), null)
            }

            outputStream?.use { os ->
                val wb = Workbook(os, "AdminApp", "1.0")
                val ws = wb.newWorksheet("Jobs")

                // Headers
                ws.value(0, 0, "Job ID")
                ws.value(0, 1, "Title")
                ws.value(0, 2, "Company")
                ws.value(0, 3, "Location")
                ws.value(0, 4, "Type")
                ws.value(0, 5, "Salary")

                // Data Rows
                jobList.forEachIndexed { index, job ->
                    val row = index + 1
                    ws.value(row, 0, job.jobId ?: "N/A")
                    ws.value(row, 1, job.title ?: "N/A")
                    ws.value(row, 2, job.companyName ?: "N/A")
                    ws.value(row, 3, job.location ?: "N/A")
                    ws.value(row, 4, job.jobType ?: "N/A")
                    ws.value(row, 5, job.salaryRange ?: "N/A")
                }

                wb.finish()

                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Job report saved to Downloads", Toast.LENGTH_LONG).show()
                }
            } ?: throw Exception("Could not open output stream")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Adapter remains the same
class JobAdapter(private val list: List<job>) : androidx.recyclerview.widget.RecyclerView.Adapter<JobAdapter.JobVH>() {
    class JobVH(val b: ItemJobAdminBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobVH {
        val b = ItemJobAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobVH(b)
    }

    override fun onBindViewHolder(holder: JobVH, position: Int) {
        val currentJob = list[position]
        holder.b.apply {
            jobTitleTv.text = currentJob.title
            companyNameTv.text = currentJob.companyName
            jobLocationTv.text = currentJob.location
            jobSalaryTv.text = currentJob.salaryRange
            jobTypeChip.text = currentJob.jobType
        }
    }

    override fun getItemCount() = list.size
}