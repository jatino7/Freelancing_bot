package com.o7solutions.freelancing_bot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.Proposal
import com.o7solutions.freelancing_bot.utils.Functions
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class ProposalAdapter(private val list: ArrayList<Proposal>) :
    RecyclerView.Adapter<ProposalAdapter.ProposalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProposalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proposal, parent, false)
        return ProposalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProposalViewHolder, position: Int) {
      holder.apply {
          description?.text = list[position].description
          time?.text = Functions.formatDateTime(list[position].timestamp)
          userName?.text = list[position].userId
          forJob?.text = "For:${list[position].forJob}"
      }
    }

    override fun getItemCount(): Int = list.size

    inner class ProposalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var description: TextView? = itemView.findViewById<TextView>(R.id.tvDescription)
        var time: TextView? = itemView.findViewById<TextView>(R.id.tvTimestamp)
        var userName: TextView? = itemView.findViewById<TextView>(R.id.tvUserEmail)
        var forJob : TextView? = itemView.findViewById<TextView>(R.id.tvFor)


    }
}
