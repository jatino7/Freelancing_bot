package com.o7solutions.freelancing_bot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.job
import com.o7solutions.freelancing_bot.utils.Functions
import kotlinx.coroutines.Job
import org.w3c.dom.Text

class HomeAdapter(val dataList: ArrayList<job>,val onClick: ItemClick):
RecyclerView.Adapter<HomeAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.job_item,parent,false)
        return ViewHolder(view)
    }



    override fun onBindViewHolder(holder: HomeAdapter.ViewHolder, position: Int) {

        val item = dataList[position]
        holder.apply {
            title.text = item.title
            description.text = item.description
            date.text = Functions.formatDate(item.timestamp)

            view.setOnClickListener {
                onClick.onClicked(position)
            }
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    inner class ViewHolder(var view: View): RecyclerView.ViewHolder(view) {
        var title = view.findViewById<TextView>(R.id.titleTextView)
        var description = view.findViewById<TextView>(R.id.descriptionTextView)
        var date = view.findViewById<TextView>(R.id.timestampTextView)
    }

    interface ItemClick {


        fun onClicked(position: Int)
    }
}