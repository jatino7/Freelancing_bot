package com.o7solutions.freelancing_bot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.Experience

class ExperienceAdapter(private val dataList: ArrayList<Experience>, private val onClick: ItemClick) :
    RecyclerView.Adapter<ExperienceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Ensure the layout name matches your file (item_experience.xml)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exprience, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        holder.apply {
            title.text = item.title
            company.text = item.companyName
            description.text = item.description

            // Format: "Jan 2022 - Present" or "Jan 2022 - Dec 2023"
            val durationText = "${item.startDate} — ${item.endDate}"
            duration.text = durationText

            // Handle Delete Click
            deleteBtn.setOnClickListener {
                onClick.onDeleteClick(position)
            }

            // Handle Item Click for Editing
            itemView.setOnClickListener {
                onClick.onItemClick(position)
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val company: TextView = view.findViewById(R.id.companyName) // Added for professional look
        val duration: TextView = view.findViewById(R.id.duration)      // Added for dates
        val description: TextView = view.findViewById(R.id.description)
        val deleteBtn: ImageButton = view.findViewById(R.id.deleteBtn) // Added for management
    }

    interface ItemClick {
        fun onItemClick(position: Int)
        fun onDeleteClick(position: Int)
    }
}