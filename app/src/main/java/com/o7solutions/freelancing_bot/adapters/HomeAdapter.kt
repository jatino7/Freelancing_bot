package com.o7solutions.freelancing_bot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.utils.Functions

class HomeAdapter(private val dataList: ArrayList<job>, private val onClick: ItemClick) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Ensure the layout name matches your XML file (e.g., item_job or job_item)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.job_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        holder.apply {
            title.text = item.title
            company.text = item.companyName

            // Combining Location and Job Type for a clean LinkedIn look
            locationInfo.text = "${item.location} (${item.jobType})"

            // Showing Salary as the prominent badge
            salary.text = if (item.salaryRange.isNotEmpty()) item.salaryRange else "Salary not disclosed"

            // Formatting timestamp (e.g., "2 hours ago" or "22 Mar 2026")
            date.text = Functions.formatDate(item.timestamp)

            // Optional: If you have image loading logic (like Glide/Coil)
            // Glide.with(view.context).load(item.companyLogoUrl).placeholder(R.drawable.work).into(jobImage)

            itemView.setOnClickListener {
                onClick.onClicked(position)
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleTextView)
        val company: TextView = view.findViewById(R.id.companyNameTextView)
        val locationInfo: TextView = view.findViewById(R.id.locationInfoTextView)
        val salary: TextView = view.findViewById(R.id.salaryTextView)
        val date: TextView = view.findViewById(R.id.timestampTextView)
        val jobImage: ImageView = view.findViewById(R.id.jobImage)
    }

    interface ItemClick {
        fun onClicked(position: Int)
    }
}