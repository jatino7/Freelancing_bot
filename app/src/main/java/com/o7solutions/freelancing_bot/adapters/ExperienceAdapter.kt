package com.o7solutions.freelancing_bot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.Experience

class ExperienceAdapter(val dataList: ArrayList<Experience>, val onClick: ItemClick) :
    RecyclerView.Adapter<ExperienceAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExperienceAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_exprience, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = dataList[position]
        holder.apply {
            title.text = item.title
            description.text = item.description


        }
    }


    override fun getItemCount(): Int {
        return dataList.size
    }


    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var title = view.findViewById<TextView>(R.id.title)
        var description = view.findViewById<TextView>(R.id.description)
    }

    interface ItemClick {


//        fun onClicked(position: Int)
    }
}
